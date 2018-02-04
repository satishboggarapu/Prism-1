package com.mikechoch.prism;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
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
    private static Context context;

    public static FirebaseUser user;
    public static HashMap user_liked_posts; // KEY: String postID   VALUE: long timestamp
    public static HashMap user_reposted_posts; // KEY: String postID   VALUE: long timestamp
    public static String user_profile_pic_uri;
    public static String username;
    public static String user_full_name;

    public CurrentUser(Context context) {
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userReference = Default.USERS_REFERENCE.child(user.getUid());
        refreshUserLikedAndRepostedPosts();
        getUserProfileDetails();
        this.context = context;
    }

    public static void refreshUserLikedAndRepostedPosts() {
        user_liked_posts = new HashMap<String, Long>();
        user_reposted_posts = new HashMap<String, Long>();
        userReference.child(Key.DB_REF_USER_LIKES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_liked_posts.putAll((Map) dataSnapshot.getValue());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        userReference.child(Key.DB_REF_USER_REPOSTS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_reposted_posts.putAll((Map) dataSnapshot.getValue());
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
                    user_profile_pic_uri = (String) dataSnapshot.child(Key.DB_REF_USER_PROFILE_PIC).getValue();
                    username = (String) dataSnapshot.child(Key.DB_REF_USER_PROFILE_USERNAME).getValue();
                    user_full_name = (String) dataSnapshot.child(Key.DB_REF_USER_PROFILE_FULL_NAME).getValue();

                    ImageView userProfilePicImageView = ((Activity) context).findViewById(R.id.profile_frag_profile_picture_image_view);;
                    TextView userUsernameTextView = ((Activity) context).findViewById(R.id.profile_frag_username_text_view);
                    TextView userFullNameTextView = ((Activity) context).findViewById(R.id.profile_frag_full_name_text_view);

                    Glide.with(context)
                            .asBitmap()
                            .load(user_profile_pic_uri)
                            .apply(new RequestOptions().fitCenter())
                            .into(new BitmapImageViewTarget(userProfilePicImageView) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                    drawable.setCircular(true);
                                    userProfilePicImageView.setImageDrawable(drawable);

                                    if (user_profile_pic_uri != null) {
                                        int whiteOutlinePadding = (int) (1 * context.getResources().getDisplayMetrics().density);
                                        userProfilePicImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                                        userProfilePicImageView.setBackground(context.getResources().getDrawable(R.drawable.circle_profile_frame));
                                    }
                                }
                            });
                    userUsernameTextView.setText(username);
                    userFullNameTextView.setText(user_full_name);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
