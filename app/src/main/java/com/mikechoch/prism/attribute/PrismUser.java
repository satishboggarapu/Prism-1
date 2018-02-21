package com.mikechoch.prism.attribute;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mikechoch on 1/30/18.
 */

public class PrismUser implements Parcelable {

    private String uid;
    private String username;
    private String fullName;
    private ProfilePicture profilePicture;
    private int followerCount;
    private int followingCount;

    public PrismUser() { }

    public PrismUser(String uid, String username, String fullName, ProfilePicture profilePicture, int followerCount, int followingCount) {
        this.uid = uid;
        this.username = username;
        this.fullName = fullName;
        this.profilePicture = profilePicture;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public ProfilePicture getProfilePicture() {
        return profilePicture;
    }

    public int getFollowerCount() {
        return followerCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setProfilePicture(ProfilePicture profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setFollowerCount(int followerCount) {
        this.followerCount = followerCount;
    }

    public void setFollowingCount(int followingCount) {
        this.followingCount = followingCount;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(username);
        dest.writeString(fullName);
        dest.writeInt(followerCount);
        dest.writeInt(followingCount);
        dest.writeParcelable(profilePicture, 0);
    }

    protected PrismUser(Parcel in) {
        uid = in.readString();
        username = in.readString();
        fullName = in.readString();
        followerCount = in.readInt();
        followingCount = in.readInt();
        profilePicture = in.readParcelable(ProfilePicture.class.getClassLoader());
    }

    public static final Creator<PrismUser> CREATOR = new Creator<PrismUser>() {
        @Override
        public PrismUser createFromParcel(Parcel in) {
            return new PrismUser(in);
        }

        @Override
        public PrismUser[] newArray(int size) {
            return new PrismUser[size];
        }
    };


}
