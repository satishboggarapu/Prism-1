package com.mikechoch.prism.attribute;

import android.os.Parcel;
import android.os.Parcelable;

public class PrismPost implements Parcelable {

    // --------------------------------------- //
    // DO NOT CHANGE ANYTHING IN THIS FILE     //
    // THESE HAVE TO BE SAME AS "POST_*" KEYS  //
    // --------------------------------------- //

    private String image;
    private String caption;
    private long timestamp;
    private String uid;


    // Attributes not saved in cloud
    private Integer likes;
    private Integer reposts;
    private String postId;
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(caption);
        dest.writeLong(timestamp);
        dest.writeString(uid);
        if (likes == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(likes);
        }
        if (reposts == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(reposts);
        }
        dest.writeParcelable(prismUser, 0);
        dest.writeString(postId);
    }

    protected PrismPost(Parcel in) {
        image = in.readString();
        caption = in.readString();
        timestamp = in.readLong();
        uid = in.readString();
        if (in.readByte() == 0) {
            likes = null;
        } else {
            likes = in.readInt();
        }
        if (in.readByte() == 0) {
            reposts = null;
        } else {
            reposts = in.readInt();
        }
        prismUser = in.readParcelable(PrismUser.class.getClassLoader());
        postId = in.readString();
    }

    public static final Creator<PrismPost> CREATOR = new Creator<PrismPost>() {
        @Override
        public PrismPost createFromParcel(Parcel in) {
            return new PrismPost(in);
        }

        @Override
        public PrismPost[] newArray(int size) {
            return new PrismPost[size];
        }
    };
}