package com.mikechoch.prism;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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

    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_upload_activity_layout);

        scale = getResources().getDisplayMetrics().density;
        int displayHeight = getWindowManager().getDefaultDisplay().getHeight();
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

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
                    uploadImageToCloud(imageDescription);
                }
            }
        });

        uploadButtonTextView = findViewById(R.id.upload_button_text_view);
        uploadButtonTextView.setTypeface(sourceSansProBold);

        imageDescriptionEditText = findViewById(R.id.image_description_edit_text);
        imageDescriptionEditText.setTypeface(sourceSansProLight);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("wallpapers");

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

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
