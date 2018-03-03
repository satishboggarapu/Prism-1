package com.mikechoch.prism.fire;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
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
import com.mikechoch.prism.R;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by parth on 1/25/18.
 */

public class CurrentUser {

    /*
     * Globals
     */
    private static FirebaseAuth auth;
    public static FirebaseUser firebaseUser;
    private static DatabaseReference userReference;
    private static DatabaseReference allPostReference;

    private static Context context;
    private static float scale;

    /**
     * Key: String postId
     * Value: long timestamp
    **/
    private static HashMap liked_posts_map;
    private static HashMap reposted_posts_map;
    private static HashMap uploaded_posts_map;

    /* ArrayList of PrismPost object for above structures */
    private static ArrayList<PrismPost> liked_posts;
    private static ArrayList<PrismPost> reposted_posts;
    private static ArrayList<PrismPost> uploaded_posts;

    /**
     * Key: String uid
     * Value: String username
     */
    static HashMap followers;
    static HashMap followings;

    public static PrismUser prismUser;

    public CurrentUser(Context context) {
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        userReference = Default.USERS_REFERENCE.child(firebaseUser.getUid());
        allPostReference = Default.ALL_POSTS_REFERENCE;

        CurrentUser.context = context;
        scale = context.getResources().getDisplayMetrics().density;

        refreshUserProfile();
    }

    /**
     * Returns True if CurrentUser is following given PrismUser
     */
    public static boolean isFollowingPrismUser(PrismUser prismUser) {
        return followings.containsKey(prismUser.getUid());
    }

    /**
     * Adds given prismUser to CurrentUser's followings HashMap
     */
    static void followUser(PrismUser prismUser) {
        followings.put(prismUser.getUid(), prismUser.getUsername());
    }


    /**
     * Removes given PrismUser from CurrentUser's followings HashMap
     */
    static void unfollowUser(PrismUser prismUser) {
        if (followings.containsKey(prismUser.getUid())) {
            followings.remove(prismUser.getUid());
        }
    }

    /**
     * Returns True if given PrismUser is a follower of CurrentUser
     */
    public static boolean isPrismUserFollower(PrismUser prismUser) {
        return followers.containsKey(prismUser.getUid());
    }


    /**
     * Returns True if CurrentUser has liked given prismPost
     */
    public static boolean hasLiked(PrismPost prismPost) {
        return liked_posts != null && liked_posts_map.containsKey(prismPost.getPostId());
    }

    /**
     * Adds prismPost to CurrentUser's liked_posts list and hashMap
     */
    static void likePost(PrismPost prismPost) {
        liked_posts.add(prismPost);
        liked_posts_map.put(prismPost.getPostId(), prismPost.getTimestamp());
    }

    /**
     * Adds list of liked prismPosts to CurrentUser's liked_posts list and hashMap
     */
    static void likePosts(ArrayList<PrismPost> likedPosts, HashMap<String, Long> likePostsMap) {
        liked_posts.addAll(likedPosts);
        liked_posts_map.putAll(likePostsMap);
    }

    /**
     * Removes prismPost from CurrentUser's liked_posts list and hashMap
     */
    static void unlikePost(PrismPost prismPost) {
        liked_posts.remove(prismPost);
        liked_posts_map.remove(prismPost.getPostId());
    }

    /**
     * Returns True if CurrentUser has reposted given prismPost
     */
    public static boolean hasReposted(PrismPost prismPost) {
        return reposted_posts_map != null && reposted_posts_map.containsKey(prismPost.getPostId());
    }

    /**
     * Adds prismPost to CurrentUser's reposted_posts list and hashMap
     */
    static void repostPost(PrismPost prismPost) {
        reposted_posts.add(prismPost);
        reposted_posts_map.put(prismPost.getPostId(), prismPost);
    }

    /**
     * Adds the list of reposted prismPosts to CurrentUser's reposted_posts list and hashMap
     */
    static void repostPosts(ArrayList<PrismPost> repostedPosts, HashMap<String, Long> repostedPostsMap) {
        reposted_posts.addAll(repostedPosts);
        reposted_posts_map.putAll(repostedPostsMap);
    }

    /**
     * Removes prismPost from CurrentUser's repost_posts list and hashMap
     */
    static void unrepostPost(PrismPost prismPost) {
        reposted_posts.remove(prismPost);
        reposted_posts_map.remove(prismPost.getPostId());
    }

    /**
     *
     * Adds prismPost to CurrentUser's uploaded_posts list and hashMap
     */
    static void uploadPost(PrismPost prismPost) {
        uploaded_posts.add(prismPost);
        uploaded_posts_map.put(prismPost, prismPost.getTimestamp());
    }

    /**
     * Adds the list of uploaded prismPosts to CurrentUser's uploaded_posts list and hashMap
     */
    static void uploadPosts(ArrayList<PrismPost> uploadedPosts, HashMap<String, Long> uploadedPostsMap) {
        uploaded_posts.addAll(uploadedPosts);
        uploaded_posts_map.putAll(uploadedPostsMap);
    }

    /**
     * Removes prismPost from CurrentUser's uploaded_posts list and hashMap
     * @param prismPost
     */
    static void deletePost(PrismPost prismPost) {
        uploaded_posts.remove(prismPost);
        uploaded_posts_map.remove(prismPost.getPostId());
    }


    /**
     * Creates prismUser for CurrentUser and refreshes/updates the
     * list of posts uploaded, liked, and reposted by CurrentUser.
     * Also fetches user's followers and followings.
     */
    public static void refreshUserProfile() {
        liked_posts = new ArrayList<>();
        reposted_posts = new ArrayList<>();
        uploaded_posts = new ArrayList<>();

        liked_posts_map = new HashMap<String, Long>();
        reposted_posts_map = new HashMap<String, Long>();
        uploaded_posts_map = new HashMap<String, Long>();

        followers = new HashMap<String, String>();
        followings = new HashMap<String, String>();

        DatabaseAction.fetchUserProfile();
    }

    /**
     * Returns list of CurrentUser.uploaded_posts
     */
    public static ArrayList<PrismPost> getUserUploads() {
        return uploaded_posts;
    }

    /**
     * Returns list of CurrentUser.liked_posts
     */
    public static ArrayList<PrismPost> getUserLikes() {
        return liked_posts;
    }

    /**
     * Returns list of CurrentUser.reposted_posts
     */
    public static ArrayList<PrismPost> getUserReposts() {
        return reposted_posts;
    }

    /**
     * TODO Mike: Can we we put this function inside InterfaceAction?
     */
    static void updateUserProfilePageUI() {
        ImageView userProfileImageView = ((Activity) context).findViewById(R.id.profile_fragment_user_profile_image_view);
        TextView userProfileTextView = ((Activity) context).findViewById(R.id.profile_fragment_user_full_name_text_view);

        // TODO: Crash on fullname using tablet
        userProfileTextView.setText(prismUser.getFullName());
        Glide.with(context)
                .asBitmap()
                .thumbnail(0.05f)
                .load(prismUser.getProfilePicture().lowResUri)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(userProfileImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (!prismUser.getProfilePicture().isDefault) {
                            int whiteOutlinePadding = (int) (1 * scale);
                            userProfileImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                            userProfileImageView.setBackground(context.getResources().getDrawable(R.drawable.circle_profile_frame));
                        } else {
                            userProfileImageView.setPadding(0, 0, 0, 0);
                            userProfileImageView.setBackground(null);
                        }

                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                        drawable.setCircular(true);
                        userProfileImageView.setImageDrawable(drawable);
                    }
                });
    }
}
