package com.mikechoch.prism;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by mikechoch on 1/21/18.
 */

public class ImageUploadActivity extends AppCompatActivity {

    private final int GALLERY_INTENT_REQUEST = 1;

    private float scale;

    private ImageView uploadedImageImageView;
    private EditText imageDescriptionEditText;
    private CardView uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_upload_activity_layout);

        scale = getResources().getDisplayMetrics().density;
        int displayHeight = getWindowManager().getDefaultDisplay().getHeight();

        uploadedImageImageView = findViewById(R.id.uploaded_image_image_view);
        uploadedImageImageView.getLayoutParams().height = (int) (displayHeight * 0.6);
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
            }
        });

        imageDescriptionEditText = findViewById(R.id.image_description_edit_text);

        selectImageFromGallery();
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
                    Uri selectedImageUri = data.getData();

                    Bitmap bitmap = null;
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        bitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    String imagePath = FileChooser.getPath(this, selectedImageUri);
                    System.out.println(imagePath);
                    bitmap = ExifUtil.rotateBitmap(imagePath, bitmap);
                    uploadedImageImageView.setImageBitmap(bitmap);
                } else {
                    finish();
                }
                break;
            default:
                break;
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
