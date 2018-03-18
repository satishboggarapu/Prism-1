package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.adapter.DisplayUsersRecyclerViewAdapter;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.R;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikechoch on 1/30/18.
 */

public class DisplayUsersActivity extends AppCompatActivity {

    /*
     * Global variables
     */
    private final static int LIKE_USERS = 0;
    private final static int REPOST_USERS = 1;
    private final static int FOLLOWER_USERS = 2;
    private final static int FOLLOWING_USERS = 3;

    private final DatabaseReference allPostsReference = Default.ALL_POSTS_REFERENCE;
    private final DatabaseReference usersReference = Default.USERS_REFERENCE;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private Toolbar toolbar;
    private TextView toolbarTextView;
    private ProgressBar likeRepostProgressBar;
    private RecyclerView usersRecyclerView;

    private DisplayUsersRecyclerViewAdapter displayUsersRecyclerViewAdapter;

    private Intent intent;
    private int activityCode;
    private String toolbarTitle;
    private ArrayList<PrismUser> prismUserArrayList;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.context_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_layout);

        // TODO better documentation and organize this method

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Initialize all UI elements
        toolbar = findViewById(R.id.toolbar);
        toolbarTextView = findViewById(R.id.toolbar_text_view);
        likeRepostProgressBar = findViewById(R.id.like_repost_progress_bar);
        usersRecyclerView = findViewById(R.id.like_repost_users_recycler_view);

        // Setup data structure to be populated with users who liked/reposted the post
        prismUserArrayList = new ArrayList<>();

        // getIntent and grab the String to populate the Toolbar title
        // This will be "Likes" or "Reposts"
        // Default being "Error"
        intent = getIntent();
        activityCode = intent.getIntExtra("UsersInt", -1);

        setupUIElements();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Use Intent to getData passed from PrismPost
     * Decide if it was a likes or reposts click and set number of
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupPage() {
        String id = intent.getStringExtra("UsersDataId");
        switch (activityCode) {
            case LIKE_USERS: {
                toolbarTitle = "Like";
                getLikedUsers(id);
                break;
            }
            case REPOST_USERS: {
                toolbarTitle = "Repost";
                getRepostedUsers(id);
                break;
            }
            case FOLLOWER_USERS: {
                toolbarTitle = "Follower";
                getFollowers(id);
                break;
            }
            case FOLLOWING_USERS: {
                toolbarTitle = "Following";
                getFollowings(id);
                break;
            }
            default: {
                break;
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Setup usersRecyclerView with a LinearLayoutManager, DefaultItemAnimator, and Adapter
     */
    private void setupLikeRepostRecyclerView() {
        // Setup the LinearLayoutManager and set it to the usersRecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(linearLayoutManager);

        // Setup the DefaultItemAnimator and set it to the usersRecyclerView
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        usersRecyclerView.setItemAnimator(defaultItemAnimator);

        // Setup the DividerItemDecoration and set it to the usersRecyclerView
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(this.getResources().getDrawable(R.drawable.recycler_view_divider));
        usersRecyclerView.addItemDecoration(dividerItemDecoration);

        // Setup the displayUsersRecyclerViewAdapter and set it to the usersRecyclerView
        displayUsersRecyclerViewAdapter = new DisplayUsersRecyclerViewAdapter(this, prismUserArrayList);
        usersRecyclerView.setAdapter(displayUsersRecyclerViewAdapter);
    }

    /**
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupUIElements() {
        setupPage();

        // Setup Typefaces for all text based UI elements
        toolbarTextView.setTypeface(sourceSansProLight);

        setupLikeRepostRecyclerView();
    }


    /**
     * Gets liked users for given postId
     * and then fetches user details for each userId
     */
    private void getLikedUsers(String postId) {
        allPostsReference.child(postId).child(Key.DB_REF_POST_LIKED_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            HashMap<String, Long> likedUsersMap = new HashMap<>();
                            likedUsersMap.putAll((Map) dataSnapshot.getValue());
                            fetchUserDetails(likedUsersMap);
                        } else {
                            Log.e(Default.TAG_DB, Message.NO_DATA);
                            finishUIActivities();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(Default.TAG_DB, Message.FETCH_USERS_FAIL, databaseError.toException());
                    }
                });
    }

    /**
     * Gets reposted users for given postId
     * and then fetches user details for each userId
     */
    private void getRepostedUsers(String postId) {
        allPostsReference.child(postId).child(Key.DB_REF_POST_REPOSTED_USERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            HashMap<String, Long> repostedUsersMap = new HashMap<>();
                            repostedUsersMap.putAll((Map) dataSnapshot.getValue());
                            fetchUserDetails(repostedUsersMap);
                        } else {
                            Log.e(Default.TAG_DB, Message.NO_DATA);
                            finishUIActivities();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(Default.TAG_DB, Message.FETCH_USERS_FAIL, databaseError.toException());
                    }
                });
    }

    /**
     * Gets given user's followings
     * and then fetches user details for each userId
     */
    private void getFollowings(String userId) {
        usersReference.child(userId).child(Key.DB_REF_USER_FOLLOWINGS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            HashMap<String, Long> userFollowings = new HashMap<>();
                            userFollowings.putAll((Map) dataSnapshot.getValue());
                            fetchUserDetails(userFollowings);
                        } else {
                            Log.e(Default.TAG_DB, Message.NO_DATA);
                            finishUIActivities();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(Default.TAG_DB, Message.FETCH_USERS_FAIL, databaseError.toException());
                    }
                });
    }

    /**
     * Gets give user's followers and then fetches user details for each userId
     */
    private void getFollowers(String userId) {
        usersReference.child(userId).child(Key.DB_REF_USER_FOLLOWERS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            HashMap<String, Long> userFollowers = new HashMap<>();
                            userFollowers.putAll((Map) dataSnapshot.getValue());
                            fetchUserDetails(userFollowers);
                        } else {
                            Log.e(Default.TAG_DB, Message.NO_DATA);
                            finishUIActivities();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(Default.TAG_DB, Message.FETCH_USERS_FAIL, databaseError.toException());
                    }
                });
    }


    /**
     * Goes over the map of users provided and pulls details for each
     * firebaseUser in the hashMap and creates a PrismUser object for each user
     */
    private void fetchUserDetails(HashMap<String, Long> usersMap) {
        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (String userId : usersMap.keySet()) {
                        if (dataSnapshot.hasChild(userId)) {
                            PrismUser prismUser = Helper.constructPrismUserObject(dataSnapshot.child(userId));
                            prismUserArrayList.add(prismUser);
                        }
                    }
                }
                finishUIActivities();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, Message.FETCH_USER_DETAILS_FAIL, databaseError.toException());
            }
        });
    }


    /**
     * TODO: @Mike any way to sexify this
     */
    private void finishUIActivities() {
        usersRecyclerView.setVisibility(View.VISIBLE);
        likeRepostProgressBar.setVisibility(View.GONE);

        displayUsersRecyclerViewAdapter.notifyDataSetChanged();

        // Once data is populated set title to the number likes or reposts
        toolbarTitle = prismUserArrayList.size() + " " + toolbarTitle;
        // Make it plural
        if (!toolbarTitle.equals("Following") && prismUserArrayList.size() > 1) {
            toolbarTitle += "s";
        }
        toolbarTextView.setText(toolbarTitle);
    }

}
