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
import com.mikechoch.prism.attribute.ProfilePicture;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.R;
import com.mikechoch.prism.adapter.LikeRepostUsersRecyclerViewAdapter;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikechoch on 1/30/18.
 */

public class LikeRepostActivity extends AppCompatActivity {

    /*
     * Global variables
     */
    private DatabaseReference databaseReference;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private Toolbar toolbar;
    private TextView toolbarTextView;
    private ProgressBar likeRepostProgressBar;
    private RecyclerView usersRecyclerView;

    private LikeRepostUsersRecyclerViewAdapter usersRecyclerViewAdapter;

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
        setContentView(R.layout.like_repost_activity_layout);
        databaseReference = Default.ALL_POSTS_REFERENCE;

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

        setupUIElements();
    }

    /**
     * Use Intent to getData passed from PrismPost
     * Decide if it was a likes or reposts click and set number of
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupToolbar() {
        // getIntent and grab the String to populate the Toolbar title
        // This will be "Likes" or "Reposts"
        // Default being "Error"
        Intent intent = getIntent();
        int activityCode = intent.getIntExtra("LikeRepostBoolean", -1);
        String postId = intent.getStringExtra("LikeRepostPostId");
        String toolbarTitle;

        switch (activityCode) {
            case 1: {
                toolbarTitle = "Likes";
                getListOfUsers(Key.DB_REF_POST_LIKED_USERS, postId);
                break;
            }
            case 0: {
                toolbarTitle = "Reposts";
                getListOfUsers(Key.DB_REF_POST_REPOSTED_USERS, postId);
                break;
            }
            default: {
                toolbarTitle = "Error";
                break;
            }
        }

        toolbar.setTitle(toolbarTitle);
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

        // Setup the usersRecyclerViewAdapter and set it to the usersRecyclerView
        usersRecyclerViewAdapter = new LikeRepostUsersRecyclerViewAdapter(this, prismUserArrayList);
        usersRecyclerView.setAdapter(usersRecyclerViewAdapter);
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
     * @param postId: postId for which information needs to be pulled
     */
    private void getListOfUsers(String DB_REF_POST_GET_USERS_KEY, String postId) {
        databaseReference.child(postId).child(DB_REF_POST_GET_USERS_KEY)
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
                            String toolbarTitle = "0 " + toolbar.getTitle();
                            toolbar.setTitle(toolbarTitle);
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
                    for (Map.Entry<String, String> entry : mapOfUsers.entrySet()) {
                        String userId = entry.getValue();
                        if (dataSnapshot.hasChild(userId)) {
                            PrismUser prismUser = Helper.constructPrismUserObject(dataSnapshot.child(userId));
                            prismUserArrayList.add(prismUser);
                        }
                    }
                } else {
                    Log.wtf(Default.TAG_DB, Message.NO_DATA);
                }
                usersRecyclerViewAdapter.notifyDataSetChanged();
                hideProgressBar();

                // Once data is populated set title to the number likes or reposts
                String toolbarTitle = prismUserArrayList.size() + " " + toolbar.getTitle();
                toolbar.setTitle(toolbarTitle);
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
