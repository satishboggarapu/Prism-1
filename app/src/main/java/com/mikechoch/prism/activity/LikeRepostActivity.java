package com.mikechoch.prism.activity;

import android.content.Intent;
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
    private Toolbar toolbar;

    private DatabaseReference databaseReference;

    private RecyclerView usersRecyclerView;
    private LikeRepostUsersRecyclerViewAdapter usersRecyclerViewAdapter;
    private ProgressBar likeRepostProgressBar;

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

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        likeRepostProgressBar = findViewById(R.id.like_repost_progress_bar);

        prismUserArrayList = new ArrayList<>();

        usersRecyclerView = findViewById(R.id.like_repost_users_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(this.getResources().getDrawable(R.drawable.recycler_view_divider));
        usersRecyclerView.setLayoutManager(linearLayoutManager);
        usersRecyclerView.setItemAnimator(defaultItemAnimator);
        usersRecyclerView.addItemDecoration(dividerItemDecoration);

        usersRecyclerViewAdapter = new LikeRepostUsersRecyclerViewAdapter(this, prismUserArrayList);
        usersRecyclerView.setAdapter(usersRecyclerViewAdapter);

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
                            usersRecyclerView.setVisibility(View.VISIBLE);
                            likeRepostProgressBar.setVisibility(View.GONE);
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
     * user in the hashMap and creates a PrismUser object for each user
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
                        PrismUser prismUser = new PrismUser();
                        prismUser.setUsername(entry.getKey());
                        prismUser.setUid(userId);
                        if (dataSnapshot.hasChild(userId)) {
                            DataSnapshot user = dataSnapshot.child(userId);
                            prismUser.setFullName((String) user.child(Key.USER_PROFILE_FULL_NAME).getValue());
                            prismUser.setProfilePicture(new ProfilePicture((String)
                                    user.child(Key.USER_PROFILE_PIC).getValue()));
                        }
                        prismUserArrayList.add(prismUser);
                    }
                } else {
                    Log.wtf(Default.TAG_DB, Message.NO_DATA);
                }
                usersRecyclerViewAdapter.notifyDataSetChanged();
                usersRecyclerView.setVisibility(View.VISIBLE);
                likeRepostProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, Message.FETCH_USER_DETAILS_FAIL, databaseError.toException());
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
