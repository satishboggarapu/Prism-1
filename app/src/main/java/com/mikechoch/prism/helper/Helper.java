package com.mikechoch.prism.helper;

import android.net.Uri;

import com.mikechoch.prism.DefaultProfilePicture;

/**
 * Created by parth on 2/6/18.
 */

public class Helper {


    /**
     *
     * @param picUri
     * @param wantLow
     * @return
     */
    public static Uri getProfilePictureUri(String picUri, boolean wantLow) {
        if (Character.isDigit(picUri.charAt(0))) {
            int profileIndex = Integer.parseInt(picUri);
            DefaultProfilePicture picture = DefaultProfilePicture.values()[profileIndex];
            return Uri.parse(wantLow ?
                    picture.getProfilePictureLow() : picture.getProfilePicture());
        }
        return Uri.parse(picUri);
    }

}
