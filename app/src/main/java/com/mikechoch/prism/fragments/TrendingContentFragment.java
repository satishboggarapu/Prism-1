package com.mikechoch.prism.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class TrendingContentFragment extends Fragment {

    private DatabaseReference databaseReference;

    private RecyclerView trendingContentRecyclerView;
    private RecyclerViewAdapter trendingContentRecyclerViewAdapter;
    private SwipeRefreshLayout trendingContentSwipeRefreshLayout;

    private ArrayList<String> popularityOrderWallpaperKeys;
    private HashMap<String, Wallpaper> wallpaperHashMap;

    private int[] swipeRefreshLayoutColors = {R.color.colorAccent};

    public static final TrendingContentFragment newInstance(int title, String message) {
        TrendingContentFragment trendingContentFragment = new TrendingContentFragment();
        Bundle bundle = new Bundle(2);
        bundle.putInt("Title", title);
        bundle.putString("Extra_Message", message);
        trendingContentFragment.setArguments(bundle);
        return trendingContentFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int title = getArguments().getInt("Title");
        String message = getArguments().getString("Extra_Message");
        popularityOrderWallpaperKeys = new ArrayList<>();
        wallpaperHashMap = new HashMap<>();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_USER_PROFILES).child(auth.getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    DataSnapshot[] dataSnapshots = {dataSnapshot};
                    new TrendingContentTask().execute(dataSnapshots);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trending_content_fragment_layout, container, false);

        trendingContentRecyclerView = view.findViewById(R.id.trending_content_recycler_view);
        trendingContentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        trendingContentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        int screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        trendingContentRecyclerViewAdapter = new RecyclerViewAdapter(getContext(), popularityOrderWallpaperKeys, wallpaperHashMap, null, screenWidth);
        trendingContentRecyclerView.setAdapter(trendingContentRecyclerViewAdapter);

        trendingContentSwipeRefreshLayout = view.findViewById(R.id.trending_content_swipe_refresh_layout);
        trendingContentSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        trendingContentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new TrendingContentTask().execute();
            }
        });

        return view;
    }

    private class TrendingContentTask extends AsyncTask<DataSnapshot, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(DataSnapshot... v) {
            if (v.length != 0) {


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
            trendingContentSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
