package com.mikechoch.prism.fragments;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.CurrentUser;
import com.mikechoch.prism.ProfilePicture;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.PrismPost;
import com.mikechoch.prism.R;
import com.mikechoch.prism.PrismPostRecyclerViewAdapter;
import com.mikechoch.prism.constants.Message;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikechoch on 1/22/18.
 */

public class MainContentFragment extends Fragment {

    /*
     * Globals
     */
    private DatabaseReference databaseReferenceAllPosts;
    private DatabaseReference usersReference;

    public static ArrayList<String> dateOrderedPrismPostKeys;
    public static HashMap<String, PrismPost> prismPostHashMap;

    private RelativeLayout noMainPostsRelativeLayout;
    private TextView noMainPostsTextView;
    private RecyclerView mainContentRecyclerView;
    private PrismPostRecyclerViewAdapter mainContentRecyclerViewAdapter;
    private ProgressBar mainContentProgress;

    private int[] swipeRefreshLayoutColors = {R.color.colorAccent};
    private SwipeRefreshLayout mainContentSwipeRefreshLayout;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private int screenWidth;
    private int screenHeight;
    private boolean isLoading = false;

    public static final MainContentFragment newInstance(int title, String message) {
        MainContentFragment mainContentFragment = new MainContentFragment();
        Bundle bundle = new Bundle(2);
        bundle.putInt("Title", title);
        bundle.putString("Extra_Message", message);
        mainContentFragment.setArguments(bundle);
        return mainContentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int title = getArguments().getInt("Title");
        String message = getArguments().getString("Extra_Message");

        screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();

        // Initialize normal and bold Prism font
        sourceSansProLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Black.ttf");

        dateOrderedPrismPostKeys = new ArrayList<>();
        prismPostHashMap = new HashMap<>();

        databaseReferenceAllPosts = Default.ALL_POSTS_REFERENCE;
        usersReference = Default.USERS_REFERENCE;
        refreshData();
    }


    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_content_fragment_layout, container, false);

        mainContentProgress = view.findViewById(R.id.main_content_progress_bar);
        noMainPostsRelativeLayout = view.findViewById(R.id.no_main_posts_relative_layout);
        noMainPostsTextView = view.findViewById(R.id.no_main_posts_text_view);
        noMainPostsTextView.setTypeface(sourceSansProLight);

        /*
         * The main purpose of this MainContentFragment is to be a Home page of the application
         * The RecyclerView being created below will show all of the most recent posts
         * The posts shown will be of people the user follows
         */
        mainContentRecyclerView = view.findViewById(R.id.main_content_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.recycler_view_divider));
        mainContentRecyclerView.setLayoutManager(linearLayoutManager);
        mainContentRecyclerView.setItemAnimator(defaultItemAnimator);
        mainContentRecyclerView.addItemDecoration(dividerItemDecoration);
        mainContentRecyclerView.setItemViewCacheSize(20);

        /*
         * The OnScrollListener is handling the toggling of the isLoading boolean
         * Bottom of the RecyclerView will set isLoading to true and fetchOldData() will be called
         * Otherwise a threshold is set to call fetchOldData() again and isLoading will become false
         * As new data is pulled this threshold is met
         * This avoids conflicts with swipe refreshing while pulling old data
         */
        mainContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && (totalItemCount - Default.IMAGE_LOAD_THRESHOLD == lastVisibleItem)) {
                    isLoading = true;
                    fetchOldData();
                } else if (totalItemCount > lastVisibleItem + Default.IMAGE_LOAD_THRESHOLD) {
                    isLoading = false;
                }
            }
        });
        mainContentRecyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) { }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                ImageView heartAnimationImageView = view.findViewById(R.id.recycler_view_like_heart);
                ImageView repostAnimationImageView = view.findViewById(R.id.recycler_view_repost_iris);
                heartAnimationImageView.setVisibility(View.INVISIBLE);
                repostAnimationImageView.setVisibility(View.INVISIBLE);
            }
        });

        mainContentRecyclerViewAdapter = new PrismPostRecyclerViewAdapter(getContext(), dateOrderedPrismPostKeys, prismPostHashMap, new int[]{screenWidth, screenHeight});
        mainContentRecyclerView.setAdapter(mainContentRecyclerViewAdapter);

        /*
         * SwipeRefreshLayout OnRefreshListener handles fetching new data from the cloud database
         * Checks that isLoading is false and the totalItemCount is > then the image threshold
         * Then will call refreshData
         * Otherwise stop refreshing
         */
        mainContentSwipeRefreshLayout = view.findViewById(R.id.main_content_swipe_refresh_layout);
        mainContentSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        mainContentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading || !(mainContentRecyclerViewAdapter.getItemCount() < Default.IMAGE_LOAD_THRESHOLD)) {
                    refreshData();
                } else {
                    mainContentSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        return view;
    }


    /**
     *  Clears the data structure and pulls ALL_POSTS info again from cloud
     *  Queries the ALL_POSTS data sorted by the post timestamp and pulls n
     *  number of posts and loads them into an ArrayList of postIds and
     *  a HashMap of PrismObjects
     */
    private void refreshData() {
        Query query = databaseReferenceAllPosts.orderByChild(Key.POST_TIMESTAMP).limitToFirst(Default.IMAGE_LOAD_COUNT);
        CurrentUser.refreshUserLinkedPosts();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                 * Notify that all RecyclerView data will be cleared and then clear all data structures
                 * Iterate through the DataSnapshot and add all new data to the data structures
                 * Notify RecyclerView after items are added to data structures
                 */
                dateOrderedPrismPostKeys.clear();
                prismPostHashMap.clear();
                mainContentRecyclerViewAdapter.notifyDataSetChanged();

                if (dataSnapshot.exists()) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        String postKey = postSnapshot.getKey();
                        if (!dateOrderedPrismPostKeys.contains(postKey)) {
                            PrismPost prismPost = getPrismPostObject(postSnapshot);
                            dateOrderedPrismPostKeys.add(postKey);
                            prismPostHashMap.put(postKey, prismPost);
                        }
                    }
                    noMainPostsRelativeLayout.setVisibility(View.GONE);
                    populateUserDetailsForAllPosts(true);
                } else {
                    Log.i(Default.TAG_DB, Message.NO_DATA);
                    noMainPostsRelativeLayout.setVisibility(View.VISIBLE);
                    mainContentSwipeRefreshLayout.setRefreshing(false);
                    mainContentProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }


    /**
     *  Pulls more data (for ALL_POSTS) from cloud, typically when user is about to
     *  reach the end of the list. It first gets the timestamp of the last post in
     *  the list and then queries more images starting from that last timestamp and
     *  appends them back to the end of the arrayList and the HashMap
     */
    private void fetchOldData() {
        String lastPostId = dateOrderedPrismPostKeys.get(dateOrderedPrismPostKeys.size() - 1);
        long lastPostTimestamp = prismPostHashMap.get(lastPostId).getTimestamp();
        databaseReferenceAllPosts
                .orderByChild(Key.POST_TIMESTAMP)
                .startAt(lastPostTimestamp)
                .limitToFirst(Default.IMAGE_LOAD_COUNT)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                String postKey = postSnapshot.getKey();
                                if (!dateOrderedPrismPostKeys.contains(postKey)) {
                                    PrismPost prismPost = getPrismPostObject(postSnapshot);
                                    dateOrderedPrismPostKeys.add(postKey);
                                    prismPostHashMap.put(postKey, prismPost);
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dateOrderedPrismPostKeys.size() > 0) {
                                                mainContentRecyclerViewAdapter
                                                        .notifyItemInserted(dateOrderedPrismPostKeys.size());
                                            }
                                        }
                                    });
                                }
                            }
                            populateUserDetailsForAllPosts(false);
                        } else {
                            Log.i(Default.TAG_DB, Message.NO_DATA);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
                    }
                });
    }

    /**
     * Takes in a dataSnapshot object and parses its contents
     * and returns a prismPost object
     */
    private PrismPost getPrismPostObject(DataSnapshot postSnapshot) {
        PrismPost prismPost = postSnapshot.getValue(PrismPost.class);
        prismPost.setLikes((int) postSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getChildrenCount());
        prismPost.setReposts((int) postSnapshot.child(Key.DB_REF_POST_REPOSTED_USERS).getChildrenCount());
        return prismPost;
    }

    /**
     * Once all posts are loaded into the prismPostHashMap,
     * this method iterates over each post, grabs user's details
     * for the post like "profilePicUriString" and "username" and
     * updates the prismPost objects in that hashMap and then
     * updates the RecyclerViewAdapter so the UI gets updated
     */
    private void populateUserDetailsForAllPosts(boolean updateRecyclerViewAdapter) {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (String postId: dateOrderedPrismPostKeys) {
                        PrismPost post = prismPostHashMap.get(postId);
                        DataSnapshot userSnapshot = dataSnapshot.child(post.getUid());
                        post.setUsername((String) userSnapshot
                                .child(Key.USER_PROFILE_USERNAME).getValue());
                        post.setUserProfilePicture(new ProfilePicture((String) userSnapshot
                                .child(Key.USER_PROFILE_PIC).getValue()));
                        prismPostHashMap.put(postId, post);
                    }
                    mainContentSwipeRefreshLayout.setRefreshing(false);

                    // gets called inside refreshData()
                    if (updateRecyclerViewAdapter) {
                        mainContentProgress.setVisibility(View.GONE);
                        mainContentRecyclerViewAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.i(Default.TAG_DB, Message.NO_DATA);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * Shortcut for toasting a bread, I mean a String message
     */
    private void toast(String bread) {
        Toast.makeText(getActivity(), bread, Toast.LENGTH_SHORT).show();
    }

}