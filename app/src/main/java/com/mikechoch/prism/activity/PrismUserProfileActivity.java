package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.R;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.attribute.ProfilePicture;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.fragments.MainContentFragment;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mikechoch on 2/16/18.
 */

public class PrismUserProfileActivity extends AppCompatActivity {

    /*
     * Globals
     */
    private DatabaseReference usersReference;
    private DatabaseReference allPostsReference;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private Toolbar toolbar;

    private SwipeRefreshLayout profileSwipeRefreshLayout;
    private ImageView userProfilePicImageView;
    private TextView followersCountTextView;
    private TextView followersLabelTextView;
    private TextView postsCountTextView;
    private TextView postsLabelTextView;
    private TextView followingCountTextView;
    private TextView followingLabelTextView;
    private TextView userUsernameTextView;
    private TextView userFullNameTextView;

    private String userUid;
    private PrismUser prismUser;
    private ArrayList<PrismPost> prismUserUploadedPostsArrayList;



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prism_user_profile_menu, menu);

        // Make the action_power and action_random_color MenuItems black
        Drawable powerDrawable = menu.findItem(R.id.action_profile_settings).getIcon();
        if (powerDrawable != null) {
            powerDrawable.mutate();
            powerDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.action_profile_settings:
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prism_user_profile_activity_layout);

        usersReference = Default.USERS_REFERENCE;
        allPostsReference = Default.ALL_POSTS_REFERENCE;

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Initialize all UI elements
        toolbar = findViewById(R.id.toolbar);

        // Initialize all UI elements
        profileSwipeRefreshLayout = findViewById(R.id.profile_swipe_refresh_layout);
        userProfilePicImageView = findViewById(R.id.profile_frag_profile_picture_image_view);
        followersCountTextView = findViewById(R.id.followers_count_text_view);
        followersLabelTextView = findViewById(R.id.followers_label_text_view);
        postsCountTextView = findViewById(R.id.posts_count_text_view);
        postsLabelTextView = findViewById(R.id.posts_label_text_view);
        followingCountTextView = findViewById(R.id.following_count_text_view);
        followingLabelTextView = findViewById(R.id.following_label_text_view);
        userUsernameTextView = findViewById(R.id.profile_frag_username_text_view);
        userFullNameTextView = findViewById(R.id.profile_frag_full_name_text_view);

        Intent intent = getIntent();
        userUid = intent.getStringExtra("PrismUserUid");
        prismUserUploadedPostsArrayList = new ArrayList<>();

        pullUserDetails();

        toast(userUid);

        setupToolbar();
        setupUIElements();
    }

    private void pullUserDetails() {

        usersReference.child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String userId = dataSnapshot.getKey();
                    String fullName = (String) dataSnapshot.child(Key.USER_PROFILE_FULL_NAME).getValue();
                    String username = (String) dataSnapshot.child(Key.USER_PROFILE_USERNAME).getValue();
                    ProfilePicture profilePicture = new ProfilePicture((String) dataSnapshot.child(Key.USER_PROFILE_PIC).getValue());
                    prismUser = new PrismUser(userId, username, fullName, profilePicture);
                    HashMap<String, Long> prismUserUploadedPostIds = new HashMap<>();
                    prismUserUploadedPostIds.putAll((Map) dataSnapshot.child(Key.DB_REF_USER_UPLOADS).getValue());

                    pullUserUploadedPrismPosts(prismUserUploadedPostIds);


                } else {
                    Log.wtf(Default.TAG_DB, "No user details exist");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, Message.FETCH_USER_DETAILS_FAIL, databaseError.toException());
            }
        });
    }

    private void pullUserUploadedPrismPosts(HashMap<String, Long> prismUserUploadedPostIds) {
        allPostsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (String postId : prismUserUploadedPostIds.keySet()) {
                        PrismPost prismPost = Helper.constructPrismPostObject(dataSnapshot.child(postId));
                        prismPost.setPrismUser(prismUser);
                        prismUserUploadedPostsArrayList.add(prismPost);
                    }

                } else {
                    Log.wtf(Default.TAG_DB, "No Posts Exist???");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(Default.TAG_DB, Message.FETCH_POST_INFO_FAIL, databaseError.toException());
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Setup the userProfilePicImageView so it is populated with a Default or custom picture
     * When clicked it will show an AlertDialog of options for changing the picture
     */
    private void setupUserProfileUIElements() {

    }
    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        // Setup Typefaces for all text based UI elements
        followersCountTextView.setTypeface(sourceSansProBold);
        followersLabelTextView.setTypeface(sourceSansProLight);
        postsCountTextView.setTypeface(sourceSansProBold);
        postsLabelTextView.setTypeface(sourceSansProLight);
        followingCountTextView.setTypeface(sourceSansProBold);
        followingLabelTextView.setTypeface(sourceSansProLight);
        userUsernameTextView.setTypeface(sourceSansProBold);
        userFullNameTextView.setTypeface(sourceSansProLight);

        setupUserProfileUIElements();
    }

    /**
     * Shortcut for displaying a Toast message
     */
    private void toast(String bread) {
        Toast.makeText(this, bread, Toast.LENGTH_SHORT).show();
    }
}
