package com.mikechoch.prism.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mikechoch.prism.R;
import com.mikechoch.prism.adapter.UserPostsColumnRecyclerViewAdapter;
import com.mikechoch.prism.attribute.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.constants.Default;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/22/18.
 */

public class LikedPostsFragment extends Fragment {

    private SwipeRefreshLayout likedPostsSwipeRefreshLayout;

    private int[] swipeRefreshLayoutColors = {R.color.colorAccent};

    public static final LikedPostsFragment newInstance() {
        LikedPostsFragment likedPostsFragment = new LikedPostsFragment();
        return likedPostsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.liked_posts_fragment_layout, container, false);

        likedPostsSwipeRefreshLayout = view.findViewById(R.id.liked_posts_swipe_refresh_layout);
        likedPostsSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        likedPostsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                likedPostsSwipeRefreshLayout.setRefreshing(false);
            }
        });

        LinearLayout userUploadedPostsLinearLayout = view.findViewById(R.id.current_user_liked_posts_linear_layout);
        userUploadedPostsLinearLayout.removeAllViews();
        userUploadedPostsLinearLayout.setWeightSum((float) Default.USER_UPLOADED_POSTS_COLUMNS);

//        ArrayList<ArrayList<PrismPost>> userUploadedPostsArrayLists = new ArrayList<>(Collections.nCopies(userUploadedColumns, new ArrayList<>()));
        // TODO: figure out how to initialize an ArrayList of ArrayLists without using while loop inside of populating for-loop
        ArrayList<ArrayList<PrismPost>> userUploadedPostsArrayLists = new ArrayList<>();
        for (int i = 0; i < CurrentUser.user_liked_posts.size(); i++) {
            while (userUploadedPostsArrayLists.size() != Default.USER_UPLOADED_POSTS_COLUMNS) {
                userUploadedPostsArrayLists.add(new ArrayList<>());
            }
            userUploadedPostsArrayLists.get((i % Default.USER_UPLOADED_POSTS_COLUMNS)).add(CurrentUser.user_liked_posts.get(i));
        }

        for (int i = 0; i < Default.USER_UPLOADED_POSTS_COLUMNS; i++) {
            LinearLayout recyclerViewLinearLayout = new LinearLayout(getActivity());
            LinearLayout.LayoutParams one_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f);
            recyclerViewLinearLayout.setLayoutParams(one_params);

            RecyclerView currentUserLikedPostsRecyclerView = (RecyclerView) LayoutInflater.from(getActivity()).inflate(R.layout.user_uploaded_posts_recycler_view_layout, null);
            LinearLayoutManager recyclerViewLinearLayoutManager = new LinearLayoutManager(getActivity());
            currentUserLikedPostsRecyclerView.setLayoutManager(recyclerViewLinearLayoutManager);
            UserPostsColumnRecyclerViewAdapter recyclerViewAdapter = new UserPostsColumnRecyclerViewAdapter(getActivity(), userUploadedPostsArrayLists.get(i));
            currentUserLikedPostsRecyclerView.setAdapter(recyclerViewAdapter);

            recyclerViewLinearLayout.addView(currentUserLikedPostsRecyclerView);
            userUploadedPostsLinearLayout.addView(recyclerViewLinearLayout);
        }

        return view;
    }

}
