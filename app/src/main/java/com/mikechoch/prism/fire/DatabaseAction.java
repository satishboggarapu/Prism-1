package com.mikechoch.prism.fire;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.mikechoch.prism.adapter.PrismPostRecyclerViewAdapter;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.fragments.MainContentFragment;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by parth on 2/25/18.
 */

public class DatabaseAction {

    // Using firebaseUser here instead of prismUser because CurrentUser.prismUser might not be created
    private static String currentUserId = CurrentUser.firebaseUser.getUid();
    private static String currentUsername = CurrentUser.firebaseUser.getDisplayName();
    private static DatabaseReference currentUserReference = Default.USERS_REFERENCE.child(currentUserId);
    private static DatabaseReference allPostsReference = Default.ALL_POSTS_REFERENCE;
    private static DatabaseReference usersReference = Default.USERS_REFERENCE;

    /**
     * Adds prismPost to CurrentUser's USER_LIKES section
     * Adds userId to prismPost's LIKED_USERS section
     * Performs like locally on CurrentUser
     */
    public static void performLike(PrismPost prismPost) {
        String postId = prismPost.getPostId();
        long timestamp = Calendar.getInstance().getTimeInMillis();
        DatabaseReference postReference = allPostsReference.child(postId);

        postReference.child(Key.DB_REF_POST_LIKED_USERS)
                .child(currentUserId)
                .setValue(currentUsername);

        currentUserReference.child(Key.DB_REF_USER_LIKES)
                .child(postId)
                .setValue(timestamp);

        CurrentUser.likePost(prismPost);
    }

    /**
     * Removes prismPost to CurrentUser's USER_LIKES section
     * Removes userId to prismPost's LIKED_USERS section
     * Performs unlike locally on CurrentUser*
     */
    public static void performUnlike(PrismPost prismPost) {
        String postId = prismPost.getPostId();
        DatabaseReference postReference = allPostsReference.child(postId);

        postReference.child(Key.DB_REF_POST_LIKED_USERS)
                .child(currentUserId)
                .removeValue();

        currentUserReference.child(Key.DB_REF_USER_LIKES)
                .child(postId)
                .removeValue();

        CurrentUser.unlikePost(prismPost);
    }

    /**
     * Adds prismPost to CurrentUser's USER_REPOSTS section
     * Adds userId to prismPost's REPOSTED_USERS section
     * Performs repost locally on CurrentUser
     */
    public static void performRepost(PrismPost prismPost) {
        String postId = prismPost.getPostId();
        long timestamp = Calendar.getInstance().getTimeInMillis();
        DatabaseReference postReference = allPostsReference.child(postId);

        postReference.child(Key.DB_REF_POST_REPOSTED_USERS)
                .child(currentUserId)
                .setValue(currentUsername);

        currentUserReference.child(Key.DB_REF_USER_REPOSTS)
                .child(postId)
                .setValue(timestamp);

        CurrentUser.repostPost(prismPost);
    }

    /**
     * Removes prismPost to CurrentUser's USER_REPOSTS section
     * Removes userId to prismPost's REPOSTED_USERS section
     * Performs unrepost locally on CurrentUser
     */
    public static void performUnrepost(PrismPost prismPost) {
        String postId = prismPost.getPostId();
        DatabaseReference postReference = allPostsReference.child(postId);

        postReference.child(Key.DB_REF_POST_REPOSTED_USERS)
                .child(currentUserId)
                .removeValue();

        currentUserReference.child(Key.DB_REF_USER_REPOSTS)
                .child(postId)
                .removeValue();

        CurrentUser.unrepostPost(prismPost);
    }

    /**
     * Removes prismPost image from Firebase Storage. When that task is
     * successfully completed, the likers and reposters for the post
     * are fetched and the postId is deleted under each liker and reposter's
     * USER_LIKES and USER_REPOSTS section. Then the post is deleted under
     * USER_UPLOADS for the post owner. And then the post itself is
     * deleted from ALL_POSTS. Finally, the mainRecyclerViewAdapter is refreshed
     */
    public static void deletePost(PrismPost prismPost) {
        FirebaseStorage.getInstance().getReferenceFromUrl(prismPost.getImage())
                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String postId = prismPost.getPostId();

                    allPostsReference.child(postId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot postSnapshot) {
                            if (postSnapshot.exists()) {

                                DeleteHelper.deleteLikedUsers(postSnapshot, prismPost);
                                DeleteHelper.deleteRepostedUsers(postSnapshot, prismPost);

                                usersReference.child(prismPost.getPrismUser().getUid())
                                        .child(Key.DB_REF_USER_UPLOADS)
                                        .child(postId).removeValue();

                                allPostsReference.child(postId).removeValue();

                                CurrentUser.deletePost(prismPost);
                                PrismPostRecyclerViewAdapter.prismPostArrayList.remove(prismPost);
                                refreshMainRecyclerViewAdapter();

                            } else {
                                Log.wtf(Default.TAG_DB, Message.NO_DATA);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            databaseError.toException().printStackTrace();
                            Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
                        }
                    });

                } else {
                    Log.e(Default.TAG_DB, Message.POST_DELETE_FAIL);
                }
            }
        });
    }

    /**
     * Adds prismUser's uid to CurrentUser's FOLLOWERS section and then
     * adds CurrentUser's uid to prismUser's FOLLOWINGS section
     */
    public static void followUser(PrismUser prismUser) {
        DatabaseReference userReference = usersReference.child(prismUser.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userReference.child(Key.DB_REF_USER_FOLLOWERS)
                            .child(CurrentUser.prismUser.getUid())
                            .setValue(CurrentUser.prismUser.getUsername());

                    currentUserReference.child(Key.DB_REF_USER_FOLLOWINGS)
                            .child(prismUser.getUid())
                            .setValue(prismUser.getUsername());

                    CurrentUser.followUser(prismUser);

                } else {
                    Log.e(Default.TAG_DB, Message.FETCH_USER_DETAILS_FAIL);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * Removes prismUser's uid from CurrentUser's FOLLOWERS section and then
     * removes CurrentUser's uid from prismUser's FOLLOWINGS section
     */
    public static void unfollowUser(PrismUser prismUser) {
        DatabaseReference userReference = usersReference.child(prismUser.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userReference.child(Key.DB_REF_USER_FOLLOWERS)
                            .child(CurrentUser.prismUser.getUid())
                            .removeValue();

                    currentUserReference.child(Key.DB_REF_USER_FOLLOWINGS)
                            .child(prismUser.getUid())
                            .removeValue();

                    CurrentUser.unfollowUser(prismUser);

                } else {
                    Log.e(Default.TAG_DB, Message.FETCH_USER_DETAILS_FAIL);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * Creates prismUser for CurrentUser
     * Then fetches CurrentUser's liked, reposted and uploaded posts
     * And then refresh the mainRecyclerViewAdapter
     */
    static void fetchUserProfile() {
        HashMap<String, Long> liked_posts_map = new HashMap<>();
        HashMap<String, Long> reposted_posts_map = new HashMap<>();
        HashMap<String, Long> uploaded_posts_map = new HashMap<>();

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usersSnapshot) {
                if (usersSnapshot.exists()) {
                    CurrentUser.prismUser = Helper.constructPrismUserObject(usersSnapshot
                            .child(CurrentUser.firebaseUser.getUid()));

                    DataSnapshot currentUserSnapshot = usersSnapshot
                            .child(CurrentUser.prismUser.getUid());

                    if (currentUserSnapshot.hasChild(Key.DB_REF_USER_LIKES)) {
                        liked_posts_map.putAll((Map) currentUserSnapshot.child(Key.DB_REF_USER_LIKES).getValue());
                    }
                    if (currentUserSnapshot.hasChild(Key.DB_REF_USER_REPOSTS)) {
                        reposted_posts_map.putAll((Map) currentUserSnapshot.child(Key.DB_REF_USER_REPOSTS).getValue());
                    }
                    if (currentUserSnapshot.hasChild(Key.DB_REF_USER_UPLOADS)) {
                        uploaded_posts_map.putAll((Map) currentUserSnapshot.child(Key.DB_REF_USER_UPLOADS).getValue());
                    }
                    if (currentUserSnapshot.hasChild(Key.DB_REF_USER_FOLLOWERS)) {
                        CurrentUser.followers.putAll((Map) currentUserSnapshot.child(Key.DB_REF_USER_FOLLOWERS).getValue());
                    }
                    if (currentUserSnapshot.hasChild(Key.DB_REF_USER_FOLLOWINGS)) {
                        CurrentUser.followings.putAll((Map) currentUserSnapshot.child(Key.DB_REF_USER_FOLLOWINGS).getValue());
                    }

                    allPostsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<PrismPost> userLikes = getListOfPrismPosts(dataSnapshot, usersSnapshot, liked_posts_map);
                            ArrayList<PrismPost> userReposts = getListOfPrismPosts(dataSnapshot, usersSnapshot, reposted_posts_map);
                            ArrayList<PrismPost> userUploads = getListOfPrismPosts(dataSnapshot, usersSnapshot, uploaded_posts_map);

                            CurrentUser.likePosts(userLikes, liked_posts_map);
                            CurrentUser.repostPosts(userReposts, reposted_posts_map);
                            CurrentUser.uploadPosts(userUploads, uploaded_posts_map);

                            // TODO: @Mike Is it ok to call these methods here?
                            CurrentUser.updateUserProfilePageUI();
                            refreshMainRecyclerViewAdapter();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(Default.TAG_DB, Message.FETCH_POST_INFO_FAIL, databaseError.toException());
                        }
                    });

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * Takes in a hashMap of prismPost postIds and also takes in dataSnapshot
     * referencing to `ALL_POSTS` and `USERS` and constructs PrismPost objects
     * for each postId in the hashMap and puts all prismPost objects in a list.
     * Gets called for getting user liked_posts, reposted_posts, and uploaded_posts
     */
    private static ArrayList<PrismPost> getListOfPrismPosts(DataSnapshot allPostsRefSnapshot,
                                                            DataSnapshot usersSnapshot,
                                                            HashMap<String, Long> mapOfPostIds) {
        ArrayList<PrismPost> listOfPrismPosts = new ArrayList<>();
        for (Object key : mapOfPostIds.keySet()) {
            String postId = (String) key;
            DataSnapshot postSnapshot = allPostsRefSnapshot.child(postId);

            if (postSnapshot.exists()) {
                PrismPost prismPost = Helper.constructPrismPostObject(postSnapshot);
                DataSnapshot userSnapshot = usersSnapshot.child(prismPost.getUid());
                PrismUser prismUser = Helper.constructPrismUserObject(userSnapshot);
                prismPost.setPrismUser(prismUser);

                listOfPrismPosts.add(prismPost);
            }
        }
        return listOfPrismPosts;
    }

    /**
     * Refresh mainContentFragment's recyclerViewAdapter
     */
    static void refreshMainRecyclerViewAdapter() {
        MainContentFragment.mainContentRecyclerViewAdapter.notifyDataSetChanged();
    }

}


class DeleteHelper {

    private static DatabaseReference usersReference = Default.USERS_REFERENCE;

    /**
     * Helper method that goes to all users who have liked the given
     * prismPost and deletes the postId under their USER_LIKES section
     */
    static void deleteLikedUsers(DataSnapshot postSnapshot, PrismPost post) {
        HashMap<String, String> likedUsers = new HashMap<>();
        if (postSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getChildrenCount() > 0) {
            likedUsers.putAll((Map) postSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getValue());
            for (String userId : likedUsers.keySet()) {
                usersReference.child(userId)
                        .child(Key.DB_REF_USER_LIKES)
                        .child(post.getPostId())
                        .removeValue();
            }
        }
    }

    /**
     * Helper method that goes to all users who have reposted the given
     * prismPost and deletes the postId under their USER_REPOSTS section
     */
    static void deleteRepostedUsers(DataSnapshot postSnapshot, PrismPost post) {
        HashMap<String, String> repostedUsers = new HashMap<>();
        if (postSnapshot.child(Key.DB_REF_POST_REPOSTED_USERS).getChildrenCount() > 0) {
            repostedUsers.putAll((Map) postSnapshot.child(Key.DB_REF_POST_REPOSTED_USERS).getValue());
            for (String userId : repostedUsers.keySet()) {
                usersReference.child(userId)
                        .child(Key.DB_REF_USER_REPOSTS)
                        .child(post.getPostId())
                        .removeValue();
            }
        }
    }

}

