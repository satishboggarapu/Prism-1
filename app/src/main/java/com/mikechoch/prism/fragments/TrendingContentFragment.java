package com.mikechoch.prism.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikechoch.prism.R;
import com.mikechoch.prism.adapter.PrismPostRecyclerViewAdapter;
import com.mikechoch.prism.attribute.PrismPost;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/22/18.
 */

public class TrendingContentFragment extends Fragment {

    private int screenWidth;
    private int screenHeight;

    private RecyclerView trendingContentRecyclerView;
    private PrismPostRecyclerViewAdapter trendingContentRecyclerViewAdapter;
    private SwipeRefreshLayout trendingContentSwipeRefreshLayout;

    private ArrayList<PrismPost> prismPostArrayList;
//    private ArrayList<String> popularityOrderedPrismPostKeys;
//    private HashMap<String, PrismPost> prismPostHashMap;

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

        screenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();

        prismPostArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trending_content_fragment_layout, container, false);

        trendingContentRecyclerView = view.findViewById(R.id.trending_content_recycler_view);
        trendingContentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        trendingContentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        trendingContentRecyclerViewAdapter = new PrismPostRecyclerViewAdapter(getContext(), prismPostArrayList, new int[]{screenWidth, screenHeight});
        trendingContentRecyclerView.setAdapter(trendingContentRecyclerViewAdapter);

        trendingContentSwipeRefreshLayout = view.findViewById(R.id.trending_content_swipe_refresh_layout);
        trendingContentSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        trendingContentSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        return view;
    }


}
