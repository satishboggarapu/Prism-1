package com.mikechoch.prism;

import android.graphics.Bitmap;

/**
 * Created by mikechoch on 1/21/18.
 */

public class UploadedImage {

    /**
     * Attributes
     */
    private Bitmap imageBitmap;
    private String imageCaption;

    /**
     * Constructor
     */
    public UploadedImage() {

    }

    /**
     * Getters
     */
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public String getImageCaption() {
        return imageCaption;
    }

    /**
     * Setters
     */
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }

    public void setImageCaption(String imageCaption) {
        this.imageCaption = imageCaption;
    }

}
