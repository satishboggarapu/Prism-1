package com.mikechoch.prism.attribute;

/**
 * Created by mikechoch on 1/31/18.
 */

public enum DefaultProfilePicture {

    DEFAULT_PROFILE_PIC_0("android.resource://com.mikechoch.prism/drawable/default_prof_0", "android.resource://com.mikechoch.prism/drawable/default_prof_0_low"),
    DEFAULT_PROFILE_PIC_1("android.resource://com.mikechoch.prism/drawable/default_prof_1", "android.resource://com.mikechoch.prism/drawable/default_prof_1_low"),
    DEFAULT_PROFILE_PIC_2("android.resource://com.mikechoch.prism/drawable/default_prof_2", "android.resource://com.mikechoch.prism/drawable/default_prof_2_low"),
    DEFAULT_PROFILE_PIC_3("android.resource://com.mikechoch.prism/drawable/default_prof_3", "android.resource://com.mikechoch.prism/drawable/default_prof_3_low"),
    DEFAULT_PROFILE_PIC_4("android.resource://com.mikechoch.prism/drawable/default_prof_4", "android.resource://com.mikechoch.prism/drawable/default_prof_4_low"),
    DEFAULT_PROFILE_PIC_5("android.resource://com.mikechoch.prism/drawable/default_prof_5", "android.resource://com.mikechoch.prism/drawable/default_prof_5_low"),
    DEFAULT_PROFILE_PIC_6("android.resource://com.mikechoch.prism/drawable/default_prof_6", "android.resource://com.mikechoch.prism/drawable/default_prof_6_low"),
    DEFAULT_PROFILE_PIC_7("android.resource://com.mikechoch.prism/drawable/default_prof_7", "android.resource://com.mikechoch.prism/drawable/default_prof_7_low"),
    DEFAULT_PROFILE_PIC_8("android.resource://com.mikechoch.prism/drawable/default_prof_8", "android.resource://com.mikechoch.prism/drawable/default_prof_8_low"),
    DEFAULT_PROFILE_PIC_9("android.resource://com.mikechoch.prism/drawable/default_prof_9", "android.resource://com.mikechoch.prism/drawable/default_prof_9_low");

    final private String profilePicture;
    final private String profilePictureLow;

    DefaultProfilePicture(String drawablePath, String drawablePathLow) {
        profilePicture = drawablePath;
        profilePictureLow = drawablePathLow;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getProfilePictureLow() {
        return profilePictureLow;
    }
}
