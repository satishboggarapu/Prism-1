package com.mikechoch.prism;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by mikechoch on 1/21/18.
 */

public class ImageUploadActivity extends AppCompatActivity {

    private final int GALLERY_INTENT_REQUEST = 1;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private Uri imageUri;
    private ImageView uploadedImageImageView;
    private EditText imageDescriptionEditText;
    private TextView uploadButtonTextView;
    private CardView uploadButton;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

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

        ;

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        scale = getResources().getDisplayMetrics().density;
        int displayHeight = getWindowManager().getDefaultDisplay().getHeight();
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        progressBar = findViewById(R.id.progress_bar);
        TextView uploadImageTitle = findViewById(R.id.uploaded_image_text_view_title);
        uploadImageTitle.setTypeface(sourceSansProLight);
        TextInputLayout textInputLayout = findViewById(R.id.image_description_title_text_input_layout);
        textInputLayout.setTypeface(sourceSansProLight);

        uploadedImageImageView = findViewById(R.id.uploaded_image_image_view);
        uploadedImageImageView.getLayoutParams().height = (int) (displayHeight * 0.6);
        uploadedImageImageView.setForeground(getResources().getDrawable(R.drawable.upload_selector));
        uploadedImageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFromGallery();
            }
        });

        uploadButton = findViewById(R.id.upload_button_card_view);
        uploadButton.setForeground(getResources().getDrawable(R.drawable.upload_selector));
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Parth add your cloud storing for the image here
                // TODO: I think we should make a custom object and store the following but not limited to:
                // String title
                // String description
                // Bitmap image
                // ... and so on
                toast("Uploading image..."); // TODO: show loading spinner in future
                final String imageDescription = imageDescriptionEditText.getText().toString().trim();
                if (!imageDescription.isEmpty()) {
                    String[] imageDescriptions = {imageDescription};
                    new ImageUploadTask().execute(imageDescriptions);
                }
            }
        });

        uploadButtonTextView = findViewById(R.id.upload_button_text_view);
        uploadButtonTextView.setTypeface(sourceSansProLight);

        imageDescriptionEditText = findViewById(R.id.image_description_edit_text);
        imageDescriptionEditText.setTypeface(sourceSansProLight);

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid());

        selectImageFromGallery();
    }

    private void uploadImageToCloud(final String imageDescription) {
        StorageReference filePath = storageReference.child("PostImage").child(imageUri.getLastPathSegment());
        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                DatabaseReference reference = databaseReference.push();
                reference.child("caption").setValue(imageDescription);
                reference.child("image").setValue(downloadUrl.toString());
                toast("Image successfully uploaded");
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                toast("Failed to upload image");
                e.printStackTrace();
            }
        });
    }

    /**
     *
     */
    private void selectImageFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), GALLERY_INTENT_REQUEST);
    }

    /**
     *
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case GALLERY_INTENT_REQUEST:
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

    private class ImageUploadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploadedImageImageView.setVisibility(View.INVISIBLE);
            imageDescriptionEditText.setVisibility(View.INVISIBLE);
            uploadButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }


        @Override
        protected Void doInBackground(String... imageDescription) {
            uploadImageToCloud(imageDescription[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
