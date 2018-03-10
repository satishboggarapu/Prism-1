package com.mikechoch.prism.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.R;
import com.mikechoch.prism.helper.BitmapHelper;
import com.mikechoch.prism.helper.FileChooser;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by mikechoch on 2/1/18.
 */

public class ProfilePictureUploadActivity extends AppCompatActivity {

    /*
     * Global variables
     */
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private Toolbar toolbar;
    private TextView toolbarTextView;
    private CropImageView uploadedProfileImageView;
    private Button saveButton;
    private ProgressBar uploadProfilePictureProgressBar;

    private Uri imageUri;
    private String imagePath;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.expense_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_picture_upload_activity_layout);

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Initialize all UI elements
        toolbar = findViewById(R.id.toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        uploadedProfileImageView = findViewById(R.id.uploaded_profile_crop_image_view);
        saveButton = findViewById(R.id.save_profile_button_card_view);
        uploadProfilePictureProgressBar = findViewById(R.id.upload_profile_picture_progress_bar);

        setupUIElements();

        selectImageFromGallery();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * When the ImageView holding the cropped image is clicked, Gallery is opened
     * Select a new image to crop for profile picture
     */
    private void setupUploadedProfileImageView() {
        uploadedProfileImageView.setForeground(getResources().getDrawable(R.drawable.image_upload_selector));
        uploadedProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });
    }

    /**
     * Setup saveButton, so once an image is selected and cropped it will return the Profile
     * The cropped image will be stored in the cloud under the user and replaced in the app UIs
     */
    private void setupSaveButton() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            saveButton.setForeground(getResources().getDrawable(R.drawable.image_upload_selector));
        }
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                 * When the saveButton is clicked, a new Intent is created
                 * This passes the uploaded image data (image and description) back to ProfileFragment
                 * Then ProfilePictureUploadActivity is finished
                 */
                new ImageUploadTask().execute();
            }
        });
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        setupToolbar();

        // Setup Typefaces for all text based UI elements
        toolbarTextView.setTypeface(sourceSansProLight);
        saveButton.setTypeface(sourceSansProLight);

        setupUploadedProfileImageView();
        setupSaveButton();
    }

    /**
     * Create an Intent to ask user to select a image they would like to upload
     */
    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select a picture"), Default.GALLERY_INTENT_REQUEST);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     *
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Default.GALLERY_INTENT_REQUEST:
                if (resultCode == RESULT_OK) {
                    imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    String imagePath = FileChooser.getPath(this, imageUri);
                    bitmap = BitmapHelper.rotateBitmap(imagePath, bitmap);
                    imageUri = getImageUri(bitmap);
                    uploadedProfileImageView.setImageBitmap(bitmap);
                } else {
                    if (uploadedProfileImageView.getCroppedImage() == null) {
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     *
     */
    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    /**
     *
     */
    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            System.out.println("Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.close();

        } catch (IOException e) {
            //System.out.println("File not found: " + e.getMessage());
            //System.out.println("Error accessing file: " + e.getMessage());

            saveButton.setVisibility(View.VISIBLE);
            uploadProfilePictureProgressBar.setVisibility(View.INVISIBLE);

            saveButton.setVisibility(View.VISIBLE);
            uploadProfilePictureProgressBar.setVisibility(View.INVISIBLE);
        }

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, pictureFile.getPath());

        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     *
     */
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
                + "/PrismProfilePictures");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        long timeStamp = new Date().getTime();
        imagePath = mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg";
        File mediaFile = new File(imagePath);
        return mediaFile;
    }

    /**
     *
     */
    private class ImageUploadTask extends AsyncTask<Void, Void, Void> {

        Bitmap profilePicture = uploadedProfileImageView.getCroppedImage();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            saveButton.setVisibility(View.INVISIBLE);
            uploadProfilePictureProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            storeImage(profilePicture);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Uri uri = getImageUri(uploadedProfileImageView.getCroppedImage());

            Intent data = new Intent();
            data.putExtra("CroppedProfilePicture", uri.toString());
            setResult(RESULT_OK, data);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

    }

    /**
     * Shortcut for displaying a Toast message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
