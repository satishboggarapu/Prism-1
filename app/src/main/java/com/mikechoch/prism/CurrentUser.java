package com.mikechoch.prism;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by parth on 1/25/18.
 */

public class CurrentUser {

    private static DatabaseReference userReference;
    private static FirebaseAuth auth;

    public static FirebaseUser user;
    public static HashMap userLikedPosts; // KEY: String postID   VALUE: long timestamp
    public static HashMap userRepostedPosts; // KEY: String postID   VALUE: long timestamp
    public static String userProfilePicUri;
    public static String username;
    public static String userFullName;

    public CurrentUser() {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userReference = Default.USERS_REFERENCE.child(user.getUid());
        refreshUserLikedAndRepostedPosts();
        getUserProfileDetails();

    }

    public static void refreshUserLikedAndRepostedPosts() {
        userLikedPosts = new HashMap<String, Long>();
        userRepostedPosts = new HashMap<String, Long>();
        userReference.child(Key.DB_REF_USER_LIKES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userLikedPosts.putAll((Map) dataSnapshot.getValue());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        userReference.child(Key.DB_REF_USER_REPOSTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userRepostedPosts.putAll((Map) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    public void getUserProfileDetails() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userProfilePicUri = (String) dataSnapshot.child(Key.DB_REF_USER_PROFILE_PIC).getValue();
                    username = (String) dataSnapshot.child(Key.DB_REF_USER_PROFILE_USERNAME).getValue();
                    userFullName = (String) dataSnapshot.child(Key.DB_REF_USER_PROFILE_FULL_NAME).getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
