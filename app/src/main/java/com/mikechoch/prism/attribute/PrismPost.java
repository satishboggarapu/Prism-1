package com.mikechoch.prism.attribute;

public class PrismPost {

    // --------------------------------------- //
    // DO NOT CHANGE ANYTHING IN THIS FILE     //
    // THESE HAVE TO BE SAME AS "POST_*" KEYS  //
    // --------------------------------------- //

    private String image;
    private String caption;
    private String postId;
    private long timestamp;
    private String uid;


    // Attributes not saved in cloud
    private Integer likes;
    private Integer reposts;
    private PrismUser prismUser;

    // Empty Constructor required by Firebase to convert DataSnapshot to PrismPost.class
    public PrismPost() { }

    // Constructor used when creating prismPost when firebaseUser uploads the image
    public PrismPost(String image, String caption, String uid, long timestamp) {
        this.image = image;
        this.caption = caption;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    // Getters
    public String getImage() {
        return image;
    }

    public String getCaption() {
        return caption;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Try to not use this if possible, use getPrismUser().getUid() instead
    // if getPrismUser() is not null;
    public String getUid() {
        return uid;
    }

    public String getPostId() {
        return postId;
    }


    // Getters for attributes not saved in cloud
    public Integer getLikes() {
        return likes;
    }

    public Integer getReposts() {
        return reposts;
    }

    public PrismUser getPrismUser() {
        return prismUser;
    }

    // Setters for attributes not saved in cloud
    public void setLikes(int likes) {
        this.likes = likes;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }

    public void setPrismUser(PrismUser prismUser) {
        this.prismUser = prismUser;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }


}