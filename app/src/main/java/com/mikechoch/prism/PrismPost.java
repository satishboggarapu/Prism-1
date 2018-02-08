package com.mikechoch.prism;

/**
 * Created by mikechoch on 1/21/18.
 */

public class PrismPost {

    // DO NOT CHANGE ANYTHING IN THIS FILE //
    // THESE HAVE TO BE SAME AS "POST_*" KEYS //
    private String image;
    private String caption;
    private String uid;
    private long timestamp;
    private String postId;

    // Attributes not saved in cloud
    private int likes;
    private int reposts;
    private String username;
    private ProfilePicture userProfilePicture;

    public PrismPost() { }

    public PrismPost(String image, String caption, String uid, long timestamp, String postId) {
        this.image = image;
        this.caption = caption;
        this.uid = uid;
        this.timestamp = timestamp;
        this.postId = postId;
    }



    public String getImage() {
        return image;
    }

    public String getCaption() {
        return caption;
    }

    public String getUid() {
        return uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getPostId() {
        return postId;
    }


    // Getters and Setters for attributes not saved in cloud
    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserProfilePicture(ProfilePicture profilePicture) {
        this.userProfilePicture = profilePicture;
    }

    public int getLikes() {
        return likes;
    }

    public int getReposts() {
        return reposts;
    }

    public String getUsername() {
        return username;
    }

    public ProfilePicture getUserProfilePicture() {
        return userProfilePicture;
    }


}