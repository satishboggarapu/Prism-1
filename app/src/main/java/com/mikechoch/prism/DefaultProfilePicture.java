package com.mikechoch.prism;

/**
 * Created by mikechoch on 1/31/18.
 */

public enum DefaultProfilePicture {

    DEFAULT_PROFILE_PIC_0("android.resource://com.mikechoch.prism/drawable/default_prof_0"),
    DEFAULT_PROFILE_PIC_1("android.resource://com.mikechoch.prism/drawable/default_prof_1"),
    DEFAULT_PROFILE_PIC_2("android.resource://com.mikechoch.prism/drawable/default_prof_2"),
    DEFAULT_PROFILE_PIC_3("android.resource://com.mikechoch.prism/drawable/default_prof_3"),
    DEFAULT_PROFILE_PIC_4("android.resource://com.mikechoch.prism/drawable/default_prof_4"),
    DEFAULT_PROFILE_PIC_5("android.resource://com.mikechoch.prism/drawable/default_prof_5"),
    DEFAULT_PROFILE_PIC_6("android.resource://com.mikechoch.prism/drawable/default_prof_6"),
    DEFAULT_PROFILE_PIC_7("android.resource://com.mikechoch.prism/drawable/default_prof_7"),
    DEFAULT_PROFILE_PIC_8("android.resource://com.mikechoch.prism/drawable/default_prof_8"),
    DEFAULT_PROFILE_PIC_9("android.resource://com.mikechoch.prism/drawable/default_prof_9");

    final private String profilePicture;

    DefaultProfilePicture(String drawablePath) {
        profilePicture = drawablePath;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
