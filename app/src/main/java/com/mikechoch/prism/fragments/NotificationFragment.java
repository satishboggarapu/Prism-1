package com.mikechoch.prism.fragments;

import android.graphics.Typeface;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.mikechoch.prism.R;
import com.mikechoch.prism.adapter.PrismNotificationRecyclerViewAdapter;
import com.mikechoch.prism.adapter.PrismPostRecyclerViewAdapter;
import com.mikechoch.prism.attribute.Notification;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/22/18.
 */

public class NotificationFragment extends Fragment {

    /*
     * Globals
     */
    private DatabaseReference databaseReferenceAllPosts;
    private DatabaseReference usersReference;

    private RelativeLayout noNotificationRelativeLayout;
    private TextView noNotificationTextView;
    private RecyclerView notificationRecyclerView;
    public static PrismNotificationRecyclerViewAdapter notificationRecyclerViewAdapter;
    private ProgressBar notificationProgressBar;

    private int[] swipeRefreshLayoutColors = {R.color.colorAccent};
    private SwipeRefreshLayout notificationSwipeRefreshLayout;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;

    public static ArrayList<Notification> notificationArrayList;

    public static final NotificationFragment newInstance() {
        NotificationFragment notificationFragment = new NotificationFragment();
        return notificationFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize normal and bold Prism font
        sourceSansProLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment_layout, container, false);

        notificationProgressBar = view.findViewById(R.id.notification_progress_bar);
        noNotificationRelativeLayout = view.findViewById(R.id.no_notification_relative_layout);
        noNotificationTextView = view.findViewById(R.id.no_notification_text_view);
        noNotificationTextView.setTypeface(sourceSansProLight);

         /*
         * The main purpose of this NotificationFragment is to hold all notifications for the user
         * The RecyclerView being created below will show all of the most recent notifications
         * The notifications shown will be for likes, reposts, and following related actions
         */
        notificationRecyclerView = view.findViewById(R.id.notification_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.recycler_view_divider));
        notificationRecyclerView.setLayoutManager(linearLayoutManager);
        notificationRecyclerView.setItemAnimator(defaultItemAnimator);
        notificationRecyclerView.addItemDecoration(dividerItemDecoration);
        notificationRecyclerView.setItemViewCacheSize(20);

        notificationArrayList = new ArrayList<>();
        notificationArrayList.add(new Notification());
        notificationArrayList.add(new Notification());
        notificationArrayList.add(new Notification());
        notificationArrayList.add(new Notification());
        notificationArrayList.add(new Notification());

        notificationRecyclerViewAdapter = new PrismNotificationRecyclerViewAdapter(getContext(), notificationArrayList, new int[]{screenWidth, screenHeight});
        notificationRecyclerView.setAdapter(notificationRecyclerViewAdapter);

        notificationSwipeRefreshLayout = view.findViewById(R.id.notification_swipe_refresh_layout);
        notificationSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        notificationSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                notificationSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }
}
