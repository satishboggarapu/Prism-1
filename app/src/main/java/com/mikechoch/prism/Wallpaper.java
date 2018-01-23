package com.mikechoch.prism;

import java.util.Objects;

/**
 * Created by mikechoch on 1/21/18.
 */

public class Wallpaper {

    private String caption;
    private String imageUri;
    private String date;
    private String time;
    private String userName;
    private String userFullName;

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getCaption() {
        return caption;

    }


    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Wallpaper(String caption, String imageUri, String date, String time, String userName, String userFullName) {
        this.caption = caption;
        this.imageUri = imageUri;
        this.date = date;
        this.time = time;
        this.userName = userName;
        this.userFullName = userFullName;
    }

    @Override
    public boolean equals(Object obj){
        System.out.println("In equals");
        if (obj instanceof Wallpaper) {
            Wallpaper w = (Wallpaper) obj;
            return (obj == this) || (w.imageUri.equals(this.imageUri));
        }
        return false;
    }

}