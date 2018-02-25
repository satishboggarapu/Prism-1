package com.mikechoch.prism.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.R;
import com.mikechoch.prism.adapter.ProfileViewPagerAdapter;
import com.mikechoch.prism.adapter.UserPostsColumnRecyclerViewAdapter;
import com.mikechoch.prism.attribute.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.Message;
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

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private int[] swipeRefreshLayoutColors = {R.color.colorAccent};
    private String[] setProfilePicStrings = {"Choose from gallery", "Take a selfie"};

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    private ImageView toolbarUserProfilePicImageView;
    private TextView toolbarUserUsernameTextView;
    private Button toolbarFollowButton;
    private ImageView accountEditInfoButton;
    private SwipeRefreshLayout profileSwipeRefreshLayout;
    private NestedScrollView profileNestedScrollView;
    private ImageView userProfilePicImageView;
    private Button followUserButton;
    private RelativeLayout followersRelativeLayout;
    private TextView followersCountTextView;
    private TextView followersLabelTextView;
    private TextView postsCountTextView;
    private TextView postsLabelTextView;
    private RelativeLayout followingRelativeLayout;
    private TextView followingCountTextView;
    private TextView followingLabelTextView;
    private TextView userUsernameTextView;
    private TextView userFullNameTextView;
    private TabLayout userPostsTabLayout;
    private ViewPager userPostsViewPager;

    private PrismUser prismUser;
    private boolean isCurrentUser;
    private ArrayList<PrismPost> prismUserUploadedPostsArrayList;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu., menu);
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

        // Get the screen density of the current phone for later UI element scaling
        scale = getResources().getDisplayMetrics().density;

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Initialize all UI elements
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar_layout);

        toolbarUserProfilePicImageView = findViewById(R.id.toolbar_user_profile_profile_picture_image_view);
        toolbarUserUsernameTextView = findViewById(R.id.toolbar_user_profile_username_text_view);
        toolbarFollowButton = findViewById(R.id.toolbar_follow_user_button);
        accountEditInfoButton = findViewById(R.id.toolbar_edit_account_information_image_view);
        profileSwipeRefreshLayout = findViewById(R.id.profile_swipe_refresh_layout);
        profileNestedScrollView = findViewById(R.id.profile_scroll_view);
        userProfilePicImageView = findViewById(R.id.user_profile_profile_picture_image_view);
        followUserButton = findViewById(R.id.follow_user_button);
        followersRelativeLayout = findViewById(R.id.followers_relative_layout);
        followersCountTextView = findViewById(R.id.followers_count_text_view);
        followersLabelTextView = findViewById(R.id.followers_label_text_view);
        postsCountTextView = findViewById(R.id.posts_count_text_view);
        postsLabelTextView = findViewById(R.id.posts_label_text_view);
        followingRelativeLayout = findViewById(R.id.following_relative_layout);
        followingCountTextView = findViewById(R.id.following_count_text_view);
        followingLabelTextView = findViewById(R.id.following_label_text_view);
        userUsernameTextView = findViewById(R.id.user_profile_username_text_view);
        userFullNameTextView = findViewById(R.id.user_profile_full_name_text_view);
        userPostsTabLayout = findViewById(R.id.current_user_profile_tab_layout);
        userPostsViewPager = findViewById(R.id.current_user_profile_view_pager);

        //
        Intent intent = getIntent();
        prismUser = intent.getParcelableExtra("PrismUser");
        prismUserUploadedPostsArrayList = new ArrayList<>();

        isCurrentUser = prismUser.getUid().equals(CurrentUser.prismUser.getUid());

        setupUIElements();
        pullUserDetails();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     *
     */
    private void pullUserDetails() {
        usersReference.child(prismUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
//                    prismUser = Helper.constructPrismUserObject(dataSnapshot);
                    HashMap<String, Long> prismUserUploadedPostIds = new HashMap<>();
                    Object data = dataSnapshot.child(Key.DB_REF_USER_UPLOADS).getValue();
                    if (data != null) {
                        prismUserUploadedPostIds.putAll((Map) data);
                    }
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

    /**
     *
     */
    private void pullUserUploadedPrismPosts(HashMap<String, Long> prismUserUploadedPostIds) {
        allPostsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (String postId : prismUserUploadedPostIds.keySet()) {
                        DataSnapshot postSnapshot = dataSnapshot.child(postId);
                        if (postSnapshot.exists()) {
                            PrismPost prismPost = Helper.constructPrismPostObject(postSnapshot);
                            prismPost.setPrismUser(prismUser);
                            prismUserUploadedPostsArrayList.add(prismPost);
                        } else {
                            // TODO LOG this: this should never happen
                            usersReference.child(prismUser.getUid())
                                    .child(Key.DB_REF_USER_UPLOADS)
                                    .child(postId).removeValue();
                        }
                    }

                    setupUserPostsUIElements();

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
    private void setupUserProfileUIElements(boolean isCurrentUser) {
        userFullNameTextView.setText(prismUser.getFullName());
        userUsernameTextView.setText(prismUser.getUsername());
        followersCountTextView.setText(String.valueOf(prismUser.getFollowerCount()));
        followersRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userFollowersIntent = new Intent(PrismUserProfileActivity.this, DisplayUsersActivity.class);
                userFollowersIntent.putExtra("UsersInt", 2);
                userFollowersIntent.putExtra("UsersDataId", prismUser.getUid());
                startActivity(userFollowersIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        followingCountTextView.setText(String.valueOf(prismUser.getFollowingCount()));
        followingRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userFollowingIntent = new Intent(PrismUserProfileActivity.this, DisplayUsersActivity.class);
                userFollowingIntent.putExtra("UsersInt", 3);
                userFollowingIntent.putExtra("UsersDataId", prismUser.getUid());
                startActivity(userFollowingIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        Glide.with(this)
                .asBitmap()
                .thumbnail(0.05f)
                .load(prismUser.getProfilePicture().hiResUri)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(userProfilePicImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (!prismUser.getProfilePicture().isDefault) {
                            int whiteOutlinePadding = (int) (2 * scale);
                            userProfilePicImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                            userProfilePicImageView.setBackground(getResources().getDrawable(R.drawable.circle_profile_frame));
                        } else {
                            userProfilePicImageView.setPadding(0, 0, 0, 0);
                            userProfilePicImageView.setBackground(null);
                        }

                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        drawable.setCircular(true);
                        userProfilePicImageView.setImageDrawable(drawable);
                    }
                });

        userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCurrentUser) {
                    AlertDialog setProfilePictureAlertDialog = createSetProfilePictureAlertDialog();
                    setProfilePictureAlertDialog.show();
                } else {
                    // TODO: show hi-res profile pic
                }
            }
        });

        Glide.with(this)
                .asBitmap()
                .thumbnail(0.05f)
                .load(prismUser.getProfilePicture().lowResUri)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(toolbarUserProfilePicImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (!prismUser.getProfilePicture().isDefault) {
                            int whiteOutlinePadding = (int) (1 * scale);
                            toolbarUserProfilePicImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                            toolbarUserProfilePicImageView.setBackground(getResources().getDrawable(R.drawable.circle_profile_frame));
                        } else {
                            toolbarUserProfilePicImageView.setPadding(0, 0, 0, 0);
                            toolbarUserProfilePicImageView.setBackground(null);
                        }

                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        drawable.setCircular(true);
                        toolbarUserProfilePicImageView.setImageDrawable(drawable);
                    }
                });

        toolbarUserUsernameTextView.setText(prismUser.getUsername());
    }

    /**
     * Create an AlertDialog for when the userProfilePicImageView is clicked
     * Gives the option to take a picture or select one from gallery
     */
    private AlertDialog createSetProfilePictureAlertDialog() {
        AlertDialog.Builder profilePictureAlertDialog = new AlertDialog.Builder(this);
        profilePictureAlertDialog.setTitle("Set profile picture");
        profilePictureAlertDialog.setItems(setProfilePicStrings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent imageUploadIntent = new Intent(PrismUserProfileActivity.this, ProfilePictureUploadActivity.class);
                        startActivityForResult(imageUploadIntent, Default.PROFILE_PIC_UPLOAD_INTENT_REQUEST_CODE);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case 1:
                        // TODO: Figure out camera feature
                        break;
                    default:
                        break;
                }
            }
        });

        return profilePictureAlertDialog.create();
    }

    /**
     *
     */
    private void setupCurrentUserProfilePage() {
        accountEditInfoButton.setVisibility(View.VISIBLE);
        setupEditAccountInformationButton();

        userPostsTabLayout.setVisibility(View.VISIBLE);
        userPostsViewPager.setVisibility(View.VISIBLE);
        setupCurrentUserPostsViewPager();
    }

    /**
     *
     */
    private void setupOtherUserProfilePage() {
        followUserButton.setVisibility(View.VISIBLE);
        setupFollowUserButton();

        profileSwipeRefreshLayout.setVisibility(View.VISIBLE);
        profileSwipeRefreshLayout.setColorSchemeResources(swipeRefreshLayoutColors);
        profileSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                profileSwipeRefreshLayout.setRefreshing(false);
            }
        });

        profileNestedScrollView.setVisibility(View.VISIBLE);

        toolbarFollowButton.setVisibility(View.VISIBLE);
        setupUserUploadedPostsRecyclerView();
    }

    /**
     *
     */
    private void setupCurrentUserPostsViewPager() {
        userPostsViewPager.setOffscreenPageLimit(2);
        userPostsViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(userPostsTabLayout));
        ProfileViewPagerAdapter userPostsViewPagerAdapter = new ProfileViewPagerAdapter(getSupportFragmentManager());
        userPostsViewPager.setAdapter(userPostsViewPagerAdapter);
        userPostsTabLayout.setupWithViewPager(userPostsViewPager);

        userPostsTabLayout.getTabAt(Default.USER_POSTS_VIEW_PAGER_POSTS).setCustomView(createTabTextView("POSTS"));
        userPostsTabLayout.getTabAt(Default.USER_POSTS_VIEW_PAGER_LIKES).setCustomView(createTabTextView("LIKES"));

        int selectedTabColor = getResources().getColor(R.color.colorAccent);
        int unselectedTabColor = Color.WHITE;
        ((TextView) userPostsTabLayout.getTabAt(userPostsTabLayout.getSelectedTabPosition()).getCustomView())
                .setTextColor(selectedTabColor);

        // Setup the tab selected, unselected, and reselected listener
        userPostsTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ((TextView) tab.getCustomView()).setTextColor(selectedTabColor);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ((TextView) tab.getCustomView()).setTextColor(unselectedTabColor);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Switch statement handing reselected tabs
                int tabPosition = tab.getPosition();
                switch (tabPosition) {
                    case 0:
                        break;

                    case 1:
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     *
     */
    private TextView createTabTextView(String tabTitle) {
        TextView postsTabTextView = new TextView(this);
        postsTabTextView.setText(tabTitle);
        postsTabTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        postsTabTextView.setTextSize(16);
        postsTabTextView.setTextColor(Color.WHITE);
        postsTabTextView.setTypeface(sourceSansProBold);
        return postsTabTextView;
    }

    /**
     *
     */
    private void setupUserUploadedPostsRecyclerView() {
        LinearLayout userUploadedPostsLinearLayout = this.findViewById(R.id.user_uploaded_posts_linear_layout);
        userUploadedPostsLinearLayout.removeAllViews();
        userUploadedPostsLinearLayout.setWeightSum((float) Default.USER_UPLOADED_POSTS_COLUMNS);

//        ArrayList<ArrayList<PrismPost>> userUploadedPostsArrayLists = new ArrayList<>(Collections.nCopies(userUploadedColumns, new ArrayList<>()));
        // TODO: figure out how to initialize an ArrayList of ArrayLists without using while loop inside of populating for-loop
        ArrayList<ArrayList<PrismPost>> userUploadedPostsArrayLists = new ArrayList<>();
        for (int i = 0; i < prismUserUploadedPostsArrayList.size(); i++) {
            while (userUploadedPostsArrayLists.size() != Default.USER_UPLOADED_POSTS_COLUMNS) {
                userUploadedPostsArrayLists.add(new ArrayList<>());
            }
            userUploadedPostsArrayLists.get((i % Default.USER_UPLOADED_POSTS_COLUMNS)).add(prismUserUploadedPostsArrayList.get(i));
        }

        for (int i = 0; i < Default.USER_UPLOADED_POSTS_COLUMNS; i++) {
            LinearLayout recyclerViewLinearLayout = new LinearLayout(this);
            LinearLayout.LayoutParams one_params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1f);
            recyclerViewLinearLayout.setLayoutParams(one_params);

            RecyclerView userUploadedPostsRecyclerView = (RecyclerView) LayoutInflater.from(this).inflate(R.layout.user_uploaded_posts_recycler_view_layout, null);
            LinearLayoutManager recyclerViewLinearLayoutManager = new LinearLayoutManager(this);
            userUploadedPostsRecyclerView.setLayoutManager(recyclerViewLinearLayoutManager);
            UserPostsColumnRecyclerViewAdapter recyclerViewAdapter = new UserPostsColumnRecyclerViewAdapter(this, userUploadedPostsArrayLists.get(i));
            userUploadedPostsRecyclerView.setAdapter(recyclerViewAdapter);

            recyclerViewLinearLayout.addView(userUploadedPostsRecyclerView);
            userUploadedPostsLinearLayout.addView(recyclerViewLinearLayout);
        }

        userUploadedPostsLinearLayout.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    private void setupAppBarLayout() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            private int scrollRange = -1;

            @Override
            public void onOffsetChanged(final AppBarLayout appBarLayout, int verticalOffset) {
                //Initialize the size of the scroll
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                float toolbarElementsAlpha = Math.abs(verticalOffset/ ((float) scrollRange));
                toolbarUserProfilePicImageView.setAlpha(toolbarElementsAlpha);
                toolbarUserUsernameTextView.setAlpha(toolbarElementsAlpha);
                toolbarFollowButton.setAlpha(toolbarElementsAlpha);

                // Check if the view is collapsed
                if (scrollRange + verticalOffset == 0) {
//                    toolbarUserProfilePicImageView.setVisibility(View.VISIBLE);
//                    toolbarUserUsernameTextView.setVisibility(View.VISIBLE);
                } else {
//                    toolbarUserProfilePicImageView.setVisibility(View.GONE);
//                    toolbarUserUsernameTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     *
     */
    private void setupEditAccountInformationButton() {
        accountEditInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editUserProfileIntent = new Intent(PrismUserProfileActivity.this, EditUserProfileActivity.class);
                startActivity(editUserProfileIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     *
     */
    private void setupFollowUserButton() {
        boolean isFollowing = CurrentUser.followings.containsKey(prismUser.getUsername());
        changeFollowButtons(isFollowing);

        followUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFollowUser();
            }
        });


        toolbarFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFollowUser();
            }
        });
    }

    /**
     *
     */
    private void changeFollowButtons(boolean isFollowing) {
        int buttonWidth = (int) (scale * (isFollowing ? 80 : 60));
        String followButtonString = isFollowing ? "Following" : "Follow";
        int followButtonInt = isFollowing ? R.drawable.button_selector_selected : R.drawable.button_selector;
        Drawable followingButtonDrawable = getResources().getDrawable(followButtonInt);
        Drawable followingToolbarButtonDrawable = getResources().getDrawable(followButtonInt);

        followUserButton.setText(followButtonString);
        followUserButton.setBackground(followingButtonDrawable);

        toolbarFollowButton.getLayoutParams().width = buttonWidth;
        toolbarFollowButton.setText(followButtonString);
        toolbarFollowButton.setBackground(followingToolbarButtonDrawable);
        toolbarFollowButton.requestLayout();
    }

    /**
     *
     */
    private void toggleFollowUser() {
        boolean performFollow = !(CurrentUser.followings.containsKey(prismUser.getUsername()));
        usersReference.child(prismUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (performFollow) {
                        usersReference.child(prismUser.getUid())
                                .child(Key.DB_REF_USER_FOLLOWERS)
                                .child(CurrentUser.prismUser.getUsername())
                                .setValue(CurrentUser.prismUser.getUid());

                        usersReference.child(CurrentUser.prismUser.getUid())
                                .child(Key.DB_REF_USER_FOLLOWINGS)
                                .child(prismUser.getUsername())
                                .setValue(prismUser.getUid());

                        // Add prismUser to local followers HashMap
                        CurrentUser.followings.put(prismUser.getUsername(), prismUser.getUid());
                        toast("Following " + prismUser.getUsername());
                    } else {
                        usersReference.child(prismUser.getUid())
                                .child(Key.DB_REF_USER_FOLLOWERS)
                                .child(CurrentUser.prismUser.getUsername())
                                .removeValue();

                        usersReference.child(CurrentUser.prismUser.getUid())
                                .child(Key.DB_REF_USER_FOLLOWINGS)
                                .child(prismUser.getUsername())
                                .removeValue();

                        // Add prismUser to local followers HashMap
                        CurrentUser.followings.remove(prismUser.getUsername());
                        toast("Unfollowed " + prismUser.getUsername());
                    }
                    changeFollowButtons(performFollow);
                } else {
                    Log.e(Default.TAG_DB, Message.FETCH_USER_DETAILS_FAIL);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.wtf(Default.TAG_DB, databaseError.getMessage(), databaseError.toException());
            }
        });
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        setupToolbar();

        // Setup Typefaces for all text based UI elements
        toolbarUserUsernameTextView.setTypeface(sourceSansProBold);
        toolbarFollowButton.setTypeface(sourceSansProLight);
        followUserButton.setTypeface(sourceSansProLight);
        followersCountTextView.setTypeface(sourceSansProBold);
        followersLabelTextView.setTypeface(sourceSansProLight);
        postsCountTextView.setTypeface(sourceSansProBold);
        postsLabelTextView.setTypeface(sourceSansProLight);
        followingCountTextView.setTypeface(sourceSansProBold);
        followingLabelTextView.setTypeface(sourceSansProLight);
        userUsernameTextView.setTypeface(sourceSansProBold);
        userFullNameTextView.setTypeface(sourceSansProLight);

        setupAppBarLayout();
        setupUserProfileUIElements(isCurrentUser);
    }

    /**
     *
     */
    private void setupUserPostsUIElements() {
        if (isCurrentUser) {
            setupCurrentUserProfilePage();
        } else {
            setupOtherUserProfilePage();
        }
    }

    /**
     * Shortcut for displaying a Toast message
     */
    private void toast(String bread) {
        Toast.makeText(this, bread, Toast.LENGTH_SHORT).show();
    }
}
