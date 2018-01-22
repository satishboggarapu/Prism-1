package com.mikechoch.prism;

import android.graphics.Bitmap;

/**
 * Created by mikechoch on 1/21/18.
 */

public class Wallpaper {

    private String imageCaption;
    private String imageUri;

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Wallpaper(String imageCaption, String imageUri) {
        this.imageCaption = imageCaption;
        this.imageUri = imageUri;
    }

    /**
     * Constructor
     */
    public Wallpaper() {

    }

    public String getImageCaption() {
        return imageCaption;
    }

    public void setImageCaption(String imageCaption) {
        this.imageCaption = imageCaption;
    }

}
