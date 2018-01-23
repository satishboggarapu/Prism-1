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

import com.mikechoch.prism.R;
import com.mikechoch.prism.RecyclerViewAdapter;
import com.mikechoch.prism.Wallpaper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikechoch on 1/22/18.
 */

public class MainContentFragment extends Fragment {

    private RecyclerView mainContentRecyclerView;
    private RecyclerViewAdapter mainContentRecyclerViewAdapter;
    private SwipeRefreshLayout mainContentSwipeRefreshLayout;

    private ArrayList<String> dataOrderWallpaperKeys;
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
        dataOrderWallpaperKeys = new ArrayList<>();
        wallpaperHashMap = new HashMap<>();

        // TODO: Create a HashMap<String, Wallpaper> from cloud database and an ArrayList<String> of keys by date order
        // TODO: Populate RecyclerViewAdapter with HashMap<String, WallPaper> and ArrayList<String>
        
        
        new MainContentTask().execute();
    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_content_fragment_layout, container, false);

        mainContentRecyclerView = view.findViewById(R.id.main_content_recycler_view);
        mainContentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mainContentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mainContentRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                ViewPager viewPager = (ViewPager) mainContentRecyclerView.getParent().getParent().getParent();
                viewPager.invalidate();
            }
        });
        mainContentRecyclerViewAdapter = new RecyclerViewAdapter(getContext(), dataOrderWallpaperKeys, wallpaperHashMap, null);
        mainContentRecyclerView.setAdapter(mainContentRecyclerViewAdapter);

        mainContentSwipeRefreshLayout = view.findViewById(R.id.main_content_swipe_refresh_layout);
        mainContentSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        mainContentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new MainContentTask().execute();
            }
        });

        return view;
    }

    private class MainContentTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... v) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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