package com.mikechoch.prism;

/**
 * Created by mikechoch on 1/21/18.
 */

public class Wallpaper {

    // DO NOT CHANGE ANYTHING IN THIS FILE //
    // THESE HAVE TO BE SAME AS "POST_*" KEYS //
    private String image;
    private String caption;
    private String username;
    private String uid;
    private long timestamp;
    private int likes;
    private String postid;

    public Wallpaper() {
    }

    public Wallpaper(String image, String caption, String username, String uid, long timestamp, int likes, String  postid) {
        this.image = image;
        this.caption = caption;
        this.username = username;
        this.uid = uid;
        this.timestamp = timestamp;
        this.likes = likes;
        this.postid = postid;
    }

    public String getImage() {
        return image;
    }

    public String getCaption() {
        return caption;
    }

    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public String getPostid() {
        return postid;
    }
}