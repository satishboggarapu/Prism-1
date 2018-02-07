package com.mikechoch.prism;

import android.net.Uri;

/**
 * Created by parth on 2/6/18.
 */

public class ProfilePicture {


    public String profilePicUriString;
    public Uri hiResUri;
    public Uri lowResUri;
    public boolean isDefault;

    ProfilePicture(String profilePicUriString) {
        this.profilePicUriString = profilePicUriString;
        // TODO: @Parth null check for users with no profile picture
        isDefault = Character.isDigit(profilePicUriString.charAt(0));
        hiResUri = getHiResProfilePicUri();
        lowResUri = getLowResProfilePicUri();
    }

    private Uri getHiResProfilePicUri() {
        if (isDefault) {
            int profileIndex = Integer.parseInt(profilePicUriString);
            DefaultProfilePicture picture = DefaultProfilePicture.values()[profileIndex];
            return Uri.parse(picture.getProfilePicture());
        }
        return Uri.parse(profilePicUriString);
    }

    private Uri getLowResProfilePicUri() {
        if (isDefault) {
            int profileIndex = Integer.parseInt(profilePicUriString);
            DefaultProfilePicture picture = DefaultProfilePicture.values()[profileIndex];
            return Uri.parse(picture.getProfilePictureLow());
        }
        return Uri.parse(profilePicUriString);
    }


}
