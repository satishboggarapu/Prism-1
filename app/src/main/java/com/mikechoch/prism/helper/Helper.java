package com.mikechoch.prism.helper;

import com.google.firebase.database.DataSnapshot;
import com.mikechoch.prism.attribute.PrismPost;
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
}
