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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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

    private DatabaseReference databaseReference;

    private RecyclerView mainContentRecyclerView;
    private RecyclerViewAdapter mainContentRecyclerViewAdapter;
    private SwipeRefreshLayout mainContentSwipeRefreshLayout;

    private ArrayList<String> dateOrderWallpaperKeys;
    private HashMap<String, Wallpaper> wallpaperHashMap;

    private int[] swipeRefreshLayoutColors = {R.color.colorAccent};

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
        dateOrderWallpaperKeys = new ArrayList<>();
        wallpaperHashMap = new HashMap<>();

        databaseReference = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_ALL_POSTS);
        refreshData();
    }


    private void fetchOldData() {
        String lastPostId = dateOrderWallpaperKeys.get(dateOrderWallpaperKeys.size() - 1);
        long lastPostTimestamp = wallpaperHashMap.get(lastPostId).getTimestamp();
        Query query = databaseReference.orderByChild("timestamp").startAt(lastPostTimestamp).limitToFirst(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot.toString());
                DataSnapshot[] dataSnapshots = {dataSnapshot};
                new FetchOldDataTask().execute(dataSnapshots);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void refreshData() {
        Query query = databaseReference.orderByChild("timestamp").limitToFirst(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                System.out.println(dataSnapshot.toString());
                DataSnapshot[] dataSnapshots = {dataSnapshot};
                new RefreshDataTask().execute(dataSnapshots);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_content_fragment_layout, container, false);

        mainContentRecyclerView = view.findViewById(R.id.main_content_recycler_view);
        mainContentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mainContentRecyclerView.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mainContentRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.recycler_view_divider));
        mainContentRecyclerView.addItemDecoration(dividerItemDecoration);
        mainContentRecyclerView.setItemViewCacheSize(20);
        mainContentRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = linearLayoutManager.getItemCount();
                int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount - 1 == lastVisibleItem) {
                    isLoading = true;
                    fetchOldData();
                } else if (totalItemCount - 1 > lastVisibleItem + 2) {
                    isLoading = false;
                }
            }
        });

        int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        mainContentRecyclerViewAdapter = new RecyclerViewAdapter(getContext(), dateOrderWallpaperKeys, wallpaperHashMap, new int[]{screenWidth, screenHeight});
        mainContentRecyclerView.setAdapter(mainContentRecyclerViewAdapter);

        mainContentSwipeRefreshLayout = view.findViewById(R.id.main_content_swipe_refresh_layout);
        mainContentSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        mainContentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: Pull data with ASync
                if (!isLoading || mainContentRecyclerViewAdapter.getItemCount() < 5) {
                    refreshData();
                } else {
                    mainContentSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });

        return view;
    }

    private class RefreshDataTask extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(DataSnapshot... snapshots) {
            // TODO: Create a HashMap<String, Wallpaper> from cloud database and an ArrayList<String> of keys by date order
            // TODO: Populate RecyclerViewAdapter with HashMap<String, WallPaper> and ArrayList<String>
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
                    dateOrderWallpaperKeys.add(postKey);
                    wallpaperHashMap.put(postKey, wallpaper);
                }

            }
        return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            mainContentSwipeRefreshLayout.setRefreshing(false);
            if (dateOrderWallpaperKeys.size() > 0) {
                mainContentRecyclerViewAdapter.notifyItemRangeChanged(0, dateOrderWallpaperKeys.size());
            }
        }
    }

    private class FetchOldDataTask extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(DataSnapshot... snapshots) {
            // TODO: Create a HashMap<String, Wallpaper> from cloud database and an ArrayList<String> of keys by date order
            // TODO: Populate RecyclerViewAdapter with HashMap<String, WallPaper> and ArrayList<String>

            for (DataSnapshot postSnapshot : snapshots[0].getChildren()) {
                String postKey = postSnapshot.getKey();
                if (!dateOrderWallpaperKeys.contains(postKey)) {
                    Wallpaper wallpaper = postSnapshot.getValue(Wallpaper.class);
                    dateOrderWallpaperKeys.add(postKey);
                    wallpaperHashMap.put(postKey, wallpaper);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainContentRecyclerViewAdapter.notifyItemInserted(dateOrderWallpaperKeys.size());
                        }
                    });
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            mainContentSwipeRefreshLayout.setRefreshing(false);
        }
    }
}