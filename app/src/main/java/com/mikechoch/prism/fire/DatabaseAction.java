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

    // using firebaseUser here instead of prismUser because CurrentUser.prismUser might not be created
    private static String currentUserId = CurrentUser.firebaseUser.getUid();
    private static String currentUsername = CurrentUser.firebaseUser.getDisplayName();
    private static DatabaseReference currentUserReference = Default.USERS_REFERENCE.child(currentUserId);
    private static DatabaseReference allPostsReference = Default.ALL_POSTS_REFERENCE;
    private static DatabaseReference usersReference = Default.USERS_REFERENCE;

    /**
     *
     * @param prismPost
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
     *
     * @param prismPost
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
     *
     * @param prismPost
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
     *
     * @param prismPost
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
     *
     * @param prismPost
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

                                PrismPostRecyclerViewAdapter.prismPostArrayList.remove(prismPost);
                                notifyDataSetChanged();

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
     *
     * @param prismUser
     */
    public static void followUser(PrismUser prismUser) {
        DatabaseReference userReference = usersReference.child(prismUser.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userReference.child(Key.DB_REF_USER_FOLLOWERS)
                            .child(CurrentUser.prismUser.getUsername())
                            .setValue(CurrentUser.prismUser.getUid());

                    userReference.child(Key.DB_REF_USER_FOLLOWINGS)
                            .child(prismUser.getUsername())
                            .setValue(prismUser.getUid());

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
     *
     * @param prismUser
     */
    public static void unfollowUser(PrismUser prismUser) {
        DatabaseReference userReference = usersReference.child(prismUser.getUid());
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userReference.child(Key.DB_REF_USER_FOLLOWERS)
                            .child(CurrentUser.prismUser.getUsername())
                            .removeValue();

                    userReference.child(Key.DB_REF_USER_FOLLOWINGS)
                            .child(prismUser.getUsername())
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
     *
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
                            ArrayList<PrismPost> userLikes = getPostDetails(dataSnapshot, usersSnapshot, liked_posts_map);
                            ArrayList<PrismPost> userReposts = getPostDetails(dataSnapshot, usersSnapshot, reposted_posts_map);
                            ArrayList<PrismPost> userUploads = getPostDetails(dataSnapshot, usersSnapshot, uploaded_posts_map);

                            CurrentUser.likePosts(userLikes, liked_posts_map);
                            CurrentUser.repostPosts(userReposts, reposted_posts_map);
                            CurrentUser.uploadPosts(userUploads, uploaded_posts_map);

                            CurrentUser.updateUserProfilePageUI();
                            notifyDataSetChanged();
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
     *
     * @param allPostsRefSnapshot
     * @param usersSnapshot
     * @param mapOfPostIds
     * @return
     */
    private static ArrayList<PrismPost> getPostDetails(DataSnapshot allPostsRefSnapshot,
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
     *
     */
    static void notifyDataSetChanged() {
        MainContentFragment.mainContentRecyclerViewAdapter.notifyDataSetChanged();
    }

}


class DeleteHelper {

    private static DatabaseReference usersReference = Default.USERS_REFERENCE;

    /**
     *
     * @param postSnapshot
     * @param post
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
     *
     * @param postSnapshot
     * @param post
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

