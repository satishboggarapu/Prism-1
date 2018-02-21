package com.mikechoch.prism.helper;

import com.google.firebase.database.DataSnapshot;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.attribute.ProfilePicture;
import com.mikechoch.prism.constants.Key;

/**
 * Created by parth on 2/16/18.
 */

public class Helper {


    /**
     * Takes in a dataSnapshot object and parses its contents
     * and returns a prismPost object
     */
    public static PrismPost constructPrismPostObject(DataSnapshot postSnapshot) {
        PrismPost prismPost = postSnapshot.getValue(PrismPost.class);
        prismPost.setPostId(postSnapshot.getKey());
        prismPost.setLikes((int) postSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getChildrenCount());
        prismPost.setReposts((int) postSnapshot.child(Key.DB_REF_POST_REPOSTED_USERS).getChildrenCount());
        return prismPost;
    }

    /**
     * Takes in userSnapshot object and parses the firebaseUser details
     * and creates a prismUser object
     * @return PrismUser object
     */
    public static PrismUser constructPrismUserObject(DataSnapshot userSnapshot) {
        PrismUser prismUser = new PrismUser();
        prismUser.setUid(userSnapshot.getKey());
        prismUser.setUsername((String) userSnapshot.child(Key.USER_PROFILE_USERNAME).getValue());
        prismUser.setFullName((String) userSnapshot.child(Key.USER_PROFILE_FULL_NAME).getValue());
        prismUser.setProfilePicture(new ProfilePicture((String) userSnapshot.child(Key.USER_PROFILE_PIC).getValue()));

        if (userSnapshot.hasChild(Key.DB_REF_USER_FOLLOWERS)) {
            prismUser.setFollowerCount((int) userSnapshot.child(Key.DB_REF_USER_FOLLOWERS).getChildrenCount());
        }
        if (userSnapshot.hasChild(Key.DB_REF_USER_FOLLOWINGS)) {
            prismUser.setFollowingCount((int) userSnapshot.child(Key.DB_REF_USER_FOLLOWINGS).getChildrenCount());
        }
        return prismUser;

    }
}
