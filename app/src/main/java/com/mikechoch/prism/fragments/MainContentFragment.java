package com.mikechoch.prism.fragments;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(Key.DB_USERS_REF).child(auth.getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot[] dataSnapshots = {dataSnapshot};
                    new MainContentTask().execute(dataSnapshots);
                }
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
        mainContentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mainContentRecyclerViewAdapter = new RecyclerViewAdapter(getContext(), dateOrderWallpaperKeys, wallpaperHashMap, null);
        mainContentRecyclerView.setAdapter(mainContentRecyclerViewAdapter);

        mainContentSwipeRefreshLayout = view.findViewById(R.id.main_content_swipe_refresh_layout);
        mainContentSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        mainContentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO: Pull data with ASync
                new MainContentTask().execute();
            }
        });

        return view;
    }

    private class MainContentTask extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(DataSnapshot... v) {
            if (v.length != 0) {
                // TODO: Create a HashMap<String, Wallpaper> from cloud database and an ArrayList<String> of keys by date order
                // TODO: Populate RecyclerViewAdapter with HashMap<String, WallPaper> and ArrayList<String>
                dateOrderWallpaperKeys.clear();
                wallpaperHashMap.clear();
                // for each user
                for (DataSnapshot dsUser : v[0].getChildren()) {

                    DataSnapshot profileSnap = dsUser.child(Key.DB_USERS_PROFILE_REF);
                    String userFullName = (String) profileSnap.child(Key.DB_USERS_PROFILE_NAME).getValue();
                    String userName = (String) profileSnap.child(Key.DB_USERS_PROFILE_USERNAME).getValue();

                    // for pics for each dsUser
                    for (DataSnapshot snapshot : dsUser.getChildren()) {
                        String postId = snapshot.getKey();
                        if (postId.equals(Key.DB_USERS_PROFILE_REF)) {
                            continue;
                        }
                        String imageUri = (String) snapshot.child(Key.POST_IMAGE_URI).getValue();
                        String caption = (String) snapshot.child(Key.POST_DESC).getValue();
                        String date = (String) snapshot.child(Key.POST_DATE).getValue();
                        String time = (String) snapshot.child(Key.POST_TIME).getValue();
                        Wallpaper wallpaper = new Wallpaper(caption, imageUri, date, time, userName, userFullName);
                        dateOrderWallpaperKeys.add(postId);
                        wallpaperHashMap.put(postId, wallpaper);
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mainContentRecyclerViewAdapter.notifyItemInserted(dateOrderWallpaperKeys.size() - 1);
                            }
                        });
                    }
                }
            } else {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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