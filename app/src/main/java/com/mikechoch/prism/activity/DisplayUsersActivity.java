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
    final private int LIKE_USERS = 0;
    final private int REPOST_USERS = 1;
    final private int FOLLOWER_USERS = 2;
    final private int FOLLOWING_USERS = 3;

    private DatabaseReference databaseReference;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private Toolbar toolbar;
    private TextView toolbarTextView;
    private ProgressBar likeRepostProgressBar;
    private RecyclerView usersRecyclerView;

    private DisplayUsersRecyclerViewAdapter displayUsersRecyclerViewAdapter;

    private int activityCode;
    private String usersDataId;
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
        Intent intent = getIntent();
        activityCode = intent.getIntExtra("UsersInt", -1);
        usersDataId = intent.getStringExtra("UsersDataId");
        databaseReference = activityCode < 2 ? Default.ALL_POSTS_REFERENCE : Default.USERS_REFERENCE;

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
    private void setupToolbar() {
        switch (activityCode) {
            case LIKE_USERS: {
                toolbarTitle = "Like";
                getListOfUsers(Key.DB_REF_POST_LIKED_USERS, usersDataId);
                break;
            }
            case REPOST_USERS: {
                toolbarTitle = "Repost";
                getListOfUsers(Key.DB_REF_POST_REPOSTED_USERS, usersDataId);
                break;
            }
            case FOLLOWER_USERS: {
                toolbarTitle = "Follower";
                getListOfUsers(Key.DB_REF_USER_FOLLOWERS, usersDataId);
                break;
            }
            case FOLLOWING_USERS: {
                toolbarTitle = "Following";
                getListOfUsers(Key.DB_REF_USER_FOLLOWINGS, usersDataId);
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
        setupToolbar();

        // Setup Typefaces for all text based UI elements
        toolbarTextView.setTypeface(sourceSansProLight);

        setupLikeRepostRecyclerView();
    }

    /**
     * Goes to the post in database and pulls list of users who have "liked" or "reposted"
     * the post and then calls getUserDetails to get further details for all those users
     * @param DB_REF_POST_GET_USERS_KEY: will be either LIKED_USERS or REPOSTED_USERS
     * @param usersDataId: usersDataId for which information needs to be pulled
     */
    private void getListOfUsers(String DB_REF_POST_GET_USERS_KEY, String usersDataId) {
        databaseReference.child(usersDataId).child(DB_REF_POST_GET_USERS_KEY)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            HashMap<String, String> mapOfUsers = new HashMap<>();
                            mapOfUsers.putAll((Map) dataSnapshot.getValue());
                            fetchUserDetails(mapOfUsers);
                        } else {
                            Log.e(Default.TAG_DB, Message.NO_DATA);
                            hideProgressBar();

                            // Once data is populated set title to the number likes or reposts
                            toolbarTitle = "0 " + toolbarTitle + (toolbarTitle.equals("Following") ? "" : "s");
                            toolbarTextView.setText(toolbarTitle);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.wtf(Default.TAG_DB, Message.FETCH_USERS_FAIL, databaseError.toException());
                    }
                });

    }

    /**
     * Goes over the list of users provided as parameter and pulls details for each
     * firebaseUser in the hashMap and creates a PrismUser object for each firebaseUser
     * @param mapOfUsers
     */
    private void fetchUserDetails(HashMap<String, String> mapOfUsers) {
        DatabaseReference usersRef = Default.USERS_REFERENCE;
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (String userId : mapOfUsers.keySet()) {
                        if (dataSnapshot.hasChild(userId)) {
                            PrismUser prismUser = Helper.constructPrismUserObject(dataSnapshot.child(userId));
                            prismUserArrayList.add(prismUser);
                        }
                    }
                } else {
                    Log.wtf(Default.TAG_DB, Message.NO_DATA);
                }
                displayUsersRecyclerViewAdapter.notifyDataSetChanged();
                hideProgressBar();

                // Once data is populated set title to the number likes or reposts
                toolbarTitle = prismUserArrayList.size() + " " + toolbarTitle +
                        (toolbarTitle.equals("Following") ? "" :
                                (prismUserArrayList.size() == 1 ? "" : "s"));
                toolbarTextView.setText(toolbarTitle);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, Message.FETCH_USER_DETAILS_FAIL, databaseError.toException());
            }
        });

    }

    /**
     * Method to hide the ProgressBar when loading likes, reposts, or error
     */
    private void hideProgressBar() {
        usersRecyclerView.setVisibility(View.VISIBLE);
        likeRepostProgressBar.setVisibility(View.GONE);
    }

}
