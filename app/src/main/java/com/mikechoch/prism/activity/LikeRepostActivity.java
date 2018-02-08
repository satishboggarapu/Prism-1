package com.mikechoch.prism.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.ProfilePicture;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.PrismUser;
import com.mikechoch.prism.R;
import com.mikechoch.prism.LikeRepostUsersRecyclerViewAdapter;

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
                            usersRecyclerView.setVisibility(View.VISIBLE);
                            likeRepostProgressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

    }

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
                            // TODO FIX AND REFACTOR THIS SHIT
                            if (user.hasChild("fullname")) {
                                prismUser.setFullName((String) user.child("fullname").getValue());
                            }
                            if (user.hasChild("profilepic")) {
                                prismUser.setProfilePicture(new ProfilePicture((String) user.child("profilepic").getValue()));
                            }
                        }
                        prismUserArrayList.add(prismUser);
                    }
                }
                usersRecyclerViewAdapter.notifyDataSetChanged();
                usersRecyclerView.setVisibility(View.VISIBLE);
                likeRepostProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}
