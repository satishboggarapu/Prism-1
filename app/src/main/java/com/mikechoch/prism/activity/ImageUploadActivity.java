package com.mikechoch.prism.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mikechoch.prism.Default;
import com.mikechoch.prism.R;
import com.mikechoch.prism.helper.ExifUtil;
import com.mikechoch.prism.helper.FileChooser;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by mikechoch on 1/21/18.
 */

public class ImageUploadActivity extends AppCompatActivity {

    /*
     * Global variables
     */
    private int screenWidth;
    private int screenHeight;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private Uri imageUri;
    private ImageView uploadedImageImageView;
    private TextInputLayout imageDescriptionTextInputLayout;
    private EditText imageDescriptionEditText;
    private TextView uploadImageTitle;
    private TextView uploadButtonTextView;
    private CardView uploadButton;
    private Toolbar toolbar;
    private TextView toolbarTextView;

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

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_upload_activity_layout);

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Setup the toolbar and back button to return to MainActivity
        toolbar = findViewById(R.id.toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        toolbarTextView.setTypeface(sourceSansProLight);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get screen height for future use
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        // Initialize text related UI elements and assign typefaces
        uploadImageTitle = findViewById(R.id.uploaded_image_text_view_title);
        uploadImageTitle.setTypeface(sourceSansProLight);
        imageDescriptionTextInputLayout = findViewById(R.id.image_description_title_text_input_layout);
        imageDescriptionTextInputLayout.setTypeface(sourceSansProLight);
        uploadButtonTextView = findViewById(R.id.upload_button_text_view);
        uploadButtonTextView.setTypeface(sourceSansProLight);
        imageDescriptionEditText = findViewById(R.id.image_description_edit_text);
        imageDescriptionEditText.setTypeface(sourceSansProLight);

        // Initialize the uploadedImageImageView and give it 60% of screen height
        uploadedImageImageView = findViewById(R.id.uploaded_image_image_view);
        uploadedImageImageView.getLayoutParams().height = (int) (screenHeight * 0.6);
        uploadedImageImageView.setForeground(getResources().getDrawable(R.drawable.image_upload_selector));
        uploadedImageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });

        // Initialize the uploadButton and setup onClickListener
        uploadButton = findViewById(R.id.upload_button_card_view);
        uploadButton.setForeground(getResources().getDrawable(R.drawable.profile_pic_upload_selector));
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                 * When the uploadButton   is clicked, a new Intent is created
                 * This passes the uploaded image data (image and description) back to MainActivity
                 * Then ImageUploadActivity is finished
                 */
                Intent data = new Intent();
                data.putExtra("ImageUri", imageUri);
                data.putExtra("ImageDescription", imageDescriptionEditText.getText().toString().trim());
                setResult(RESULT_OK, data);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                /*
                 * Old method of getting image in cloud, not connected to MainActivity
                 * Bad experience for user since they would have to hold on ImageUploadActivity
                 */
//                new ImageUploadTask().execute();
            }
        });

        // Ask user to select an image to upload from phone gallery
        selectImageFromGallery();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
                    bitmap = ExifUtil.rotateBitmap(imagePath, bitmap);
                    imageUri = getImageUri(bitmap);
                    uploadedImageImageView.setImageBitmap(bitmap);
                } else {
                    if (uploadedImageImageView.getDrawable() == null) {
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
     * Shortcut for toasting a message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
