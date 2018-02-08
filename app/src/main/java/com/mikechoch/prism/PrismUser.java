package com.mikechoch.prism;

/**
 * Created by mikechoch on 1/30/18.
 */

public class PrismUser {

    private String uid;
    private String username;
    private String fullName;
    private ProfilePicture profilePicture;

    public PrismUser() {

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

}
