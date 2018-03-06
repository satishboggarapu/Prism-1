package com.mikechoch.prism.fragments;

import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikechoch.prism.R;
import com.mikechoch.prism.adapter.UserPostsColumnRecyclerViewAdapter;
import com.mikechoch.prism.fire.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.constants.Default;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/22/18.
 */

public class LikedPostsFragment extends Fragment {

    /*
     * Globals
     */
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private SwipeRefreshLayout likedPostsSwipeRefreshLayout;
    private NestedScrollView liekdPostsNestedScrollView;
    private LinearLayout userLikedPostsLinearLayout;

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

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Black.ttf");

        likedPostsSwipeRefreshLayout = view.findViewById(R.id.liked_posts_swipe_refresh_layout);
        liekdPostsNestedScrollView = view.findViewById(R.id.liked_posts_nested_scroll_view);
        userLikedPostsLinearLayout = view.findViewById(R.id.current_user_liked_posts_linear_layout);

        setupUIElements();

        return view;
    }

    /**
     *
     */
    private void setupUploadedRepostedSwipeRefreshLayout() {
        likedPostsSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        likedPostsSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                likedPostsSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     *
     */
    private void setupLikedRecyclerViewColumns() {
        userLikedPostsLinearLayout.removeAllViews();
        userLikedPostsLinearLayout.setWeightSum((float) Default.USER_UPLOADED_POSTS_COLUMNS);
        ArrayList<PrismPost> userLikedPosts = CurrentUser.getUserLikes();
        if (userLikedPosts != null) {
//        ArrayList<ArrayList<PrismPost>> userLikedPostsArrayLists = new ArrayList<>(Collections.nCopies(userUploadedColumns, new ArrayList<>()));
            // TODO: figure out how to initialize an ArrayList of ArrayLists without using while loop inside of populating for-loop
            ArrayList<ArrayList<PrismPost>> userLikedPostsArrayLists = new ArrayList<>();
            for (int i = 0; i < userLikedPosts.size(); i++) {
                while (userLikedPostsArrayLists.size() != Default.USER_UPLOADED_POSTS_COLUMNS) {
                    userLikedPostsArrayLists.add(new ArrayList<>());
                }
                userLikedPostsArrayLists.get((i % Default.USER_UPLOADED_POSTS_COLUMNS)).add(userLikedPosts.get(i));
            }

            for (int i = 0; i < Default.USER_UPLOADED_POSTS_COLUMNS; i++) {
                LinearLayout recyclerViewLinearLayout = new LinearLayout(getActivity());
                LinearLayout.LayoutParams linearLayoutLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                recyclerViewLinearLayout.setLayoutParams(linearLayoutLayoutParams);

                RecyclerView currentUserLikedPostsRecyclerView = (RecyclerView) LayoutInflater.from(getActivity()).inflate(R.layout.user_posts_column_recycler_view, null);
                LinearLayoutManager recyclerViewLinearLayoutManager = new LinearLayoutManager(getActivity());
                currentUserLikedPostsRecyclerView.setLayoutManager(recyclerViewLinearLayoutManager);
                // TODO app crashes here if user hasn't liked any pics (userLikedPostsArrayLists.size() == 0)
                UserPostsColumnRecyclerViewAdapter recyclerViewAdapter = new UserPostsColumnRecyclerViewAdapter(getActivity(), userLikedPostsArrayLists.get(i));
                currentUserLikedPostsRecyclerView.setAdapter(recyclerViewAdapter);

                recyclerViewLinearLayout.addView(currentUserLikedPostsRecyclerView);
                userLikedPostsLinearLayout.addView(recyclerViewLinearLayout);
            }
        } else {
            View noPostsView = LayoutInflater.from(getActivity()).inflate(R.layout.no_posts_user_profile_layout, null, false);

            Drawable noPostsDrawable = getResources().getDrawable(R.drawable.no_liked_posts_icon);
            ImageView noPostsImageView = noPostsView.findViewById(R.id.no_posts_image_view);
            noPostsImageView.setImageDrawable(noPostsDrawable);

            TextView noPostsTextView = noPostsView.findViewById(R.id.no_posts_text_view);
            noPostsTextView.setTypeface(sourceSansProLight);
            noPostsTextView.setText("No liked posts");

            userLikedPostsLinearLayout.addView(noPostsView);
        }
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        setupUploadedRepostedSwipeRefreshLayout();
        setupLikedRecyclerViewColumns();
    }

}
