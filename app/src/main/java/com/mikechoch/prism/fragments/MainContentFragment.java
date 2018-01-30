package com.mikechoch.prism.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.CurrentUser;
import com.mikechoch.prism.Default;
import com.mikechoch.prism.Key;
import com.mikechoch.prism.R;
import com.mikechoch.prism.RecyclerViewAdapter;
import com.mikechoch.prism.Wallpaper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikechoch on 1/22/18.
 */

public class MainContentFragment extends Fragment {

    /*
     * Global variables
     */
    private DatabaseReference databaseReference;

    private ArrayList<String> dateOrderWallpaperKeys;
    private HashMap<String, Wallpaper> wallpaperHashMap;

    private RecyclerView mainContentRecyclerView;
    private RecyclerViewAdapter mainContentRecyclerViewAdapter;
    private ProgressBar mainContentProgress;

    private int[] swipeRefreshLayoutColors = {R.color.colorAccent};
    private SwipeRefreshLayout mainContentSwipeRefreshLayout;

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

        dateOrderWallpaperKeys = new ArrayList<>();
        wallpaperHashMap = new HashMap<>();

        databaseReference = Default.ALL_POSTS_REFERENCE;
        refreshData();
    }


    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_content_fragment_layout, container, false);

        mainContentProgress = getActivity().findViewById(R.id.main_content_progress_bar);

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
            public void onChildViewAttachedToWindow(View view) {
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
                ImageView heartAnimationImageView = view.findViewById(R.id.recycler_view_like_heart);
                heartAnimationImageView.setVisibility(View.INVISIBLE);
                ImageView repostAnimationImageView = view.findViewById(R.id.recycler_view_repost_iris);
                repostAnimationImageView.setVisibility(View.INVISIBLE);
            }
        });

        mainContentRecyclerViewAdapter = new RecyclerViewAdapter(getContext(), dateOrderWallpaperKeys, wallpaperHashMap, new int[]{screenWidth, screenHeight});
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
     * Calls the RefreshDataTask after checking if more data exists in the cloud database
     */
    private void refreshData() {
        Query query = databaseReference.orderByChild(Key.POST_TIMESTAMP).limitToFirst(Default.IMAGE_LOAD_COUNT);
        CurrentUser.refreshUserLikedPosts();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    System.out.println(dataSnapshot.toString());
                    DataSnapshot[] dataSnapshots = {dataSnapshot};

                    new RefreshDataTask().execute(dataSnapshots);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    /**
     * AsyncTask for retrieving most recent data when you open the app or swipe refresh
     */
    private class RefreshDataTask extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(DataSnapshot... snapshots) {
            /*
             * Notify that all RecyclerView data will be cleared and then clear all data structures
             * Iterate through the DataSnapshot and add all new data to the data structures
             * Notify RecyclerView after items are added to data structures
             */
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dateOrderWallpaperKeys.size() > 0) {
                        mainContentRecyclerViewAdapter.notifyItemRangeRemoved(0, dateOrderWallpaperKeys.size());
                    }
                }
            });
            dateOrderWallpaperKeys.clear();
            wallpaperHashMap.clear();

            for (DataSnapshot postSnapshot : snapshots[0].getChildren()) {
                String postKey = postSnapshot.getKey();
                if (!dateOrderWallpaperKeys.contains(postKey)) {
                    Wallpaper wallpaper = postSnapshot.getValue(Wallpaper.class);
                    wallpaper.setLikes((int) postSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getChildrenCount());
                    dateOrderWallpaperKeys.add(postKey);
                    wallpaperHashMap.put(postKey, wallpaper);
                }

            }
        return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            // Stop refreshing since AsyncTask is finished
            mainContentProgress.setVisibility(View.GONE);
            mainContentSwipeRefreshLayout.setRefreshing(false);
            mainContentRecyclerViewAdapter.notifyDataSetChanged();
        }
    }


    /**
     * Calls the FetchOldDataTask after checking if more data exists in the cloud database
     */
    private void fetchOldData() {
        String lastPostId = dateOrderWallpaperKeys.get(dateOrderWallpaperKeys.size() - 1);
        long lastPostTimestamp = wallpaperHashMap.get(lastPostId).getTimestamp();
        Query query = databaseReference.orderByChild(Key.POST_TIMESTAMP).startAt(lastPostTimestamp).limitToFirst(Default.IMAGE_LOAD_COUNT);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    System.out.println(dataSnapshot.toString());
                    DataSnapshot[] dataSnapshots = {dataSnapshot};
                    new FetchOldDataTask().execute(dataSnapshots);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    /**
     * AsyncTask for retrieving older data as you scroll in the RecyclerView
     */
    private class FetchOldDataTask extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(DataSnapshot... snapshots) {
            /*
             * Iterate through the DataSnapshot and add all older data to the data structures
             * Notify RecyclerView after items are added to data structures
             */
            for (DataSnapshot postSnapshot : snapshots[0].getChildren()) {
                String postKey = postSnapshot.getKey();
                if (!dateOrderWallpaperKeys.contains(postKey)) {
                    Wallpaper wallpaper = postSnapshot.getValue(Wallpaper.class);
                    wallpaper.setLikes((int) postSnapshot.child(Key.DB_REF_POST_LIKED_USERS).getChildrenCount());
                    dateOrderWallpaperKeys.add(postKey);
                    wallpaperHashMap.put(postKey, wallpaper);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dateOrderWallpaperKeys.size() > 0) {
                                mainContentRecyclerViewAdapter.notifyItemInserted(dateOrderWallpaperKeys.size());
                            }
                        }
                    });
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            // Stop refreshing since AsyncTask is finished
            mainContentSwipeRefreshLayout.setRefreshing(false);
        }
    }
}