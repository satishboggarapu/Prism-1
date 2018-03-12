package com.mikechoch.prism.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Transition;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.mikechoch.prism.InterfaceAction;
import com.mikechoch.prism.ToolbarPullDownLayout;
import com.mikechoch.prism.R;
import com.mikechoch.prism.fire.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.fire.DatabaseAction;
import com.mikechoch.prism.helper.Helper;

import java.text.DecimalFormat;


/**
 * Created by mikechoch on 2/19/18.
 */

public class PrismPostDetailActivity extends AppCompatActivity {

    /*
     * Globals
     */
    private int scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED;
    private int noScrollFlags = 0;

    private String[] morePostOptionsCurrentUser = {"Report post", "Share", "Delete"};
    private String[] morePostOptions = {"Report post", "Share"};

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;

    private String postId;
    private String postDate;
    private Integer likeCount;
    private Integer repostCount;
    private boolean isPostLiked;
    private boolean isPostReposted;

    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;

    private ToolbarPullDownLayout toolbarPullDownLayout;
    private CoordinatorLayout prismPostDetailCoordinateLayout;
    private NestedScrollView prismPostDetailNestedScrollView;
    private ScrollView prismPostDetailScrollView;
    private RelativeLayout prismPostDetailsRelativeLayout;
    private ImageView likeActionButton;
    private TextView likesCountTextView;
    private ImageView repostActionButton;
    private TextView repostCountTextView;
    private LinearLayout detailImageLinearLayout;
    private ImageView detailImageView;
    private RelativeLayout userRelativeLayout;
    private ImageView detailUserProfilePictureImageView;
    private TextView detailUsernameTextView;
    private TextView detailPrismPostDateTextView;
    private TextView detailPrismPostDescriptionTextView;
    private TextView detailPrismPostTagsTextView;
    private ImageView collapsingToolbarCollapseUpButton;
    private ImageView collapsingToolbarDragArrow;

    private PrismPost prismPost;

    private ScaleGestureDetector mScaleDetector;
    private float startDistanceChange;
    private float totalDistanceChange;
    private boolean isZooming = false;

    private Handler collapseUpButtonHandler = new Handler();
    private Runnable showCollapseUpButtonRunnable;
    private Runnable hideCollapseUpButtonRunnable;
    private Handler dragArrowHandler = new Handler();
    private Runnable showDragArrowRunnable;
    private Runnable hideDragArrowRunnable;
    private boolean shouldCollapseUpButtonShow = false;
    private Menu menu;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.prism_post_detail_menu, menu);
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            menuItem.setVisible(false);
            Drawable drawable = menuItem.getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.prism_post_detail_action_more:
                boolean isCurrentUserThePostCreator = CurrentUser.firebaseUser.getUid().equals(prismPost.getPrismUser().getUid());
                AlertDialog morePrismPostAlertDialog = InterfaceAction.createMorePrismPostAlertDialog(this, prismPost, isCurrentUserThePostCreator);
                morePrismPostAlertDialog.show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prism_post_detail_activity_layout);

        // Get the screen density of the current phone for later UI element scaling
        scale = getResources().getDisplayMetrics().density;

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Get the screen width and height of the current phone
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        // Initialize all UI elements
        appBarLayout = findViewById(R.id.prism_post_detail_app_bar_layout);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        toolbarPullDownLayout = findViewById(R.id.pull_down_relative_layout);
        prismPostDetailCoordinateLayout = findViewById(R.id.prism_post_detail_coordinate_layout);
        prismPostDetailNestedScrollView = findViewById(R.id.prism_post_detail_nested_scroll_view);
        prismPostDetailScrollView = findViewById(R.id.prism_post_detail_scroll_view);
        prismPostDetailsRelativeLayout = findViewById(R.id.prism_post_detail_relative_layout);
        likeActionButton = findViewById(R.id.prism_post_detail_like_action_button);
        likesCountTextView = findViewById(R.id.prism_post_detail_like_count);
        repostActionButton = findViewById(R.id.prism_post_detail_repost_action_button);
        repostCountTextView = findViewById(R.id.prism_post_detail_repost_count);
        detailImageLinearLayout = findViewById(R.id.prism_post_detail_image_linear_layout);
        detailImageView = findViewById(R.id.prism_post_detail_image_view);
        userRelativeLayout = findViewById(R.id.prism_post_detail_user_relative_layout);
        detailUserProfilePictureImageView = findViewById(R.id.prism_post_detail_user_profile_picture_image_view);
        detailUsernameTextView = findViewById(R.id.prism_post_detail_username_text_view);
        detailPrismPostDateTextView = findViewById(R.id.prism_post_detail_date_text_view);
        detailPrismPostDescriptionTextView = findViewById(R.id.prism_post_description);
        detailPrismPostTagsTextView = findViewById(R.id.prism_post_tags);
        collapsingToolbarCollapseUpButton = findViewById(R.id.collapsing_toolbar_collapse_up_button);
        collapsingToolbarDragArrow = findViewById(R.id.collapsing_toolbar_drag_arrow);

        Bundle extras = getIntent().getExtras();
        prismPost = extras.getParcelable("PrismPostDetail");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = extras.getString("PrismPostDetailTransitionName");
            detailImageView.setTransitionName(imageTransitionName);
        }

        postId = this.prismPost.getPostId();
        postDate = Helper.getFancyDateDifferenceString(prismPost.getTimestamp() * -1);
        likeCount = this.prismPost.getLikes();
        repostCount = this.prismPost.getReposts();
        isPostLiked = CurrentUser.hasLiked(prismPost);
        isPostReposted = CurrentUser.hasReposted(prismPost);

        if (likeCount == null) likeCount = 0;
        if (repostCount == null) repostCount = 0;

        setupUIElements();

        Transition sharedElementEnterTransition = getWindow().getSharedElementEnterTransition();
        sharedElementEnterTransition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(Transition transition) {

            }

            @Override
            public void onTransitionEnd(Transition transition) {
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem menuItem = menu.getItem(i);
                    menuItem.setVisible(true);
                }

                showCollapseUpButton(0);
                showDragArrow(0);
                hideCollapseUpButton(4000);
                hideDragArrow(3000);
            }

            @Override
            public void onTransitionCancel(Transition transition) {

            }

            @Override
            public void onTransitionPause(Transition transition) {

            }

            @Override
            public void onTransitionResume(Transition transition) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * A method to find height of the status bar
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * A method to find height of the action bar
     */
    private int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * A method to find height of the status bar
     */
    private int getBottomNavigationBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     *
     */
    private void setupAppBarLayout() {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

//                System.out.println(Math.abs(verticalOffset));
//                System.out.println(appBarLayout.getTotalScrollRange());

                if ((Math.abs(verticalOffset) - appBarLayout.getTotalScrollRange()) == 0) {
                    // Collapsed
//                    toast("Collapsed");
                    hideCollapseUpButton(0);
                } else if (Math.abs(verticalOffset) == 0) {
                    // Expanded
//                    toast("Expanded");
                    if (shouldCollapseUpButtonShow) {
                        shouldCollapseUpButtonShow = false;
                        showCollapseUpButton(0);
                        hideCollapseUpButton(4000);
                    }
                } else {
                    // Between
//                    toast("Between");
                    shouldCollapseUpButtonShow = true;
                    hideCollapseUpButton(0);
                }
            }
        });
    }

    /**
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupToolbar() {
        toolbar.setTitle("");
        toolbar.bringToFront();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     *
     */
    private void setupStatusBar() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Make the height of the ToolbarPullDownLayout the height of the action bar
     * Give the ToolbarPullDownLayout the parent view and all children scroll views
     */
    private void setupToolbarPullDownLayout() {
        toolbarPullDownLayout.getLayoutParams().height = getActionBarHeight();
        toolbarPullDownLayout.addParentView(this, prismPostDetailCoordinateLayout);
        ViewGroup[] scrollViews = {prismPostDetailNestedScrollView, prismPostDetailScrollView};
        toolbarPullDownLayout.addScrollViews(scrollViews);
    }

    /**
     *
     */
    private void setupPrismPostImageView() {
        supportStartPostponedEnterTransition();

        Glide.with(this)
                .asBitmap()
                .load(prismPost.getImage())
                .apply(new RequestOptions().fitCenter())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        // Set the height of the appBarLayout, detailImageView, and toolbar
                        // This ensures proper UI response when scrolling the image and info window
                        appBarLayout.getLayoutParams().height = resource.getHeight();
                        detailImageView.getLayoutParams().height = resource.getHeight();

                        // Calculate the PrismPost info window height
                        int userInfoHeight = detailUserProfilePictureImageView.getHeight() +
                                detailPrismPostDescriptionTextView.getHeight() +
                                detailPrismPostTagsTextView.getHeight() +
                                prismPostDetailNestedScrollView.getPaddingTop() +
                                prismPostDetailNestedScrollView.getPaddingBottom();

                        // Check that the image height is larger or equal to actual screen height
                        // If so, set ScaleType to CENTER_CROP
                        // Otherwise, set ScaleType to FIT_START
                        boolean isLongPortraitImage = resource.getHeight() >= screenHeight;
                        detailImageView.setScaleType(isLongPortraitImage ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_START);

                        // Check that the image and info window height is larger or equal to actual screen height
                        // If so, enable collapsing toolbar using scroll flags
                        // Otherwise, disable collapsing toolbar using scroll flags
                        boolean isScrollImage = (resource.getHeight() + userInfoHeight) >= screenHeight;
                        int toolbarHeight = isScrollImage ? (screenHeight - getBottomNavigationBarHeight() - userInfoHeight) : resource.getHeight();
                        boolean isToolbarHeightNegative = toolbarHeight <= getActionBarHeight();
//                        toolbar.getLayoutParams().height = isToolbarHeightNegative ? getActionBarHeight() : toolbarHeight;

                        int toolbarTopMarginPadding = getStatusBarHeight();
                        if (isScrollImage) {
                            toolbar.getLayoutParams().height = getActionBarHeight();
                            int toolbarBottomMargin = (isToolbarHeightNegative ? getActionBarHeight() : toolbarHeight) - getActionBarHeight();
                            CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
                            params.setMargins(0, toolbarTopMarginPadding, 0, toolbarBottomMargin);
                            toolbar.setLayoutParams(params);
                        } else {
                            toolbar.getLayoutParams().height = getStatusBarHeight() + getActionBarHeight();
                            toolbar.setPadding(0, toolbarTopMarginPadding, 0, 0);
                        }

                        // Set scroll flags for collapsingToolbarLayout containing the PrismPost image
                        AppBarLayout.LayoutParams collapsingToolbarLayoutLayoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
                        collapsingToolbarLayoutLayoutParams.setScrollFlags(isScrollImage ? scrollFlags : noScrollFlags);
                        collapsingToolbarLayout.setLayoutParams(collapsingToolbarLayoutLayoutParams);

                        startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(detailImageView);

        mScaleDetector = new ScaleGestureDetector(this, new MyPinchListener());
        detailImageLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        System.out.println("DOWN");
                        break;
                    case MotionEvent.ACTION_UP:
//                        System.out.println("UP");
                        isZooming = false;
                        detailImageView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150);
                        totalDistanceChange = 0;
                        toolbarPullDownLayout.disable(false);
                        prismPostDetailScrollView.requestDisallowInterceptTouchEvent(false);
                        prismPostDetailNestedScrollView.requestDisallowInterceptTouchEvent(false);
                        break;
                }

                if (event.getPointerCount() == 2 && !isZooming) {
                    toolbarPullDownLayout.disable(true);
                    prismPostDetailScrollView.requestDisallowInterceptTouchEvent(true);
                    prismPostDetailNestedScrollView.requestDisallowInterceptTouchEvent(true);
                    isZooming = true;
                    float firstTouchX = event.getX(0);
                    float firstTouchY = event.getY(0);
                    float secondTouchX = event.getX(1);
                    float secondTouchY = event.getY(1);

                    float pivotPointX;
                    if (firstTouchX < secondTouchX) {
                        pivotPointX = firstTouchX + Math.abs(firstTouchX - secondTouchX);
                    } else {
                        pivotPointX = firstTouchX - Math.abs(firstTouchX - secondTouchX);
                    }

                    float pivotPointY;
                    if (firstTouchY < secondTouchY) {
                        pivotPointY = firstTouchY + Math.abs(firstTouchY - secondTouchY);
                    } else {
                        pivotPointY = firstTouchY - Math.abs(firstTouchY - secondTouchY);
                    }
                    detailImageView.setPivotX(pivotPointX);
                    detailImageView.setPivotY(pivotPointY);

                    double distanceX = Math.pow(Math.abs(firstTouchX - secondTouchX), 2);
                    double distanceY = Math.pow(Math.abs(firstTouchY - secondTouchY), 2);
                    startDistanceChange = (float) Math.sqrt(distanceX + distanceY);
                }

                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    /**
     *
     */
    public class MyPinchListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
//            Log.d("TAG", "PINCH! OUCH!");
            totalDistanceChange += (detector.getCurrentSpan() - detector.getPreviousSpan());
//            System.out.println(startDistanceChange);
//            System.out.println(totalDistanceChange);
            DecimalFormat df = new DecimalFormat("0.##");
            float imageScale = Float.parseFloat(df.format((double) ((startDistanceChange + totalDistanceChange) / startDistanceChange)));
//            System.out.println(imageScale);
            if (imageScale >= 1) {
                detailImageView.setScaleX(imageScale);
                detailImageView.setScaleY(imageScale);
            }
            return true;
        }
    }

    /**
     *
     */
    private void setupPrismPostUserInfo() {
        userRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToUserProfileActivity();
            }
        });

        Glide.with(this)
                .asBitmap()
                .load(prismPost.getPrismUser().getProfilePicture().lowResUri)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(detailUserProfilePictureImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (!prismPost.getPrismUser().getProfilePicture().isDefault) {
                            int whiteOutlinePadding = (int) (1 * scale);
                            detailUserProfilePictureImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                            detailUserProfilePictureImageView.setBackground(getResources().getDrawable(R.drawable.circle_profile_frame));
                        } else {
                            detailUserProfilePictureImageView.setPadding(0, 0, 0, 0);
                            detailUserProfilePictureImageView.setBackground(null);
                        }

                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        drawable.setCircular(true);
                        detailUserProfilePictureImageView.setImageDrawable(drawable);
                    }
                });

        detailUsernameTextView.setText(prismPost.getPrismUser().getUsername());
        detailPrismPostDateTextView.setText(Helper.getFancyDateDifferenceString(prismPost.getTimestamp() * -1));
        detailPrismPostDescriptionTextView.setText(prismPost.getCaption());
        detailPrismPostTagsTextView.setText(Html.fromHtml(
                "<u>#burger</u> " +
                        "<u>#delicous</u> " +
                        "<u>#foodporn</u> " +
                        "<u>#inandout</u> " +
                        "<u>#fries</u> " +
                        "<u>#carkeys</u> " +
                        "<u>#amazing</u>"));
    }

    /**
     * Intent from the current clicked PrismPost user to their PrismUserProfileActivity
     */
    private void intentToUserProfileActivity() {
        Intent prismUserProfileIntent = new Intent(PrismPostDetailActivity.this, PrismUserProfileActivity.class);
        prismUserProfileIntent.putExtra("PrismUser", prismPost.getPrismUser());
        startActivity(prismUserProfileIntent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     *
     */
    private void setupLikeActionButton() {
        InterfaceAction.setupLikeActionButton(this, null, likeActionButton, isPostLiked);
        likeActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleLikeButtonClick();
            }
        });

        likesCountTextView.setText(String.valueOf(likeCount));
        likesCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userLikesIntent = new Intent(PrismPostDetailActivity.this, DisplayUsersActivity.class);
                userLikesIntent.putExtra("UsersInt", 0);
                userLikesIntent.putExtra("UsersDataId", prismPost.getPostId());
                startActivity(userLikesIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * Check liked_posts_map HashMap if it contains the postId or not. If it contains
     * the postId, then firebaseUser has already liked the post and perform UNLIKE operation
     * If it doesn't exist, firebaseUser has not liked it yet, and perform LIKE operation
     * Operation LIKE (performLIKE = true): does 3 things. First it adds the the firebaseUser's
     * uid to the LIKED_USERS table under the post. Then it adds the postId to the
     * USER_LIKES table under the firebaseUser. Then it adds the postId and timestamp to the
     * local liked_posts_map HashMap so that recycler view can update
     * Operation UNLIKE (performLike = false): undoes above 3 things
     * TODO: update comments
     */
    private void handleLikeButtonClick() {
        boolean performLike = !CurrentUser.hasLiked(prismPost);
        performUIActivitiesForLike(performLike);

        if (performLike) {
            DatabaseAction.performLike(prismPost);
        } else {
            DatabaseAction.performUnlike(prismPost);
        }
    }

    /**
     *
     * @param performLike
     */
    private void performUIActivitiesForLike(boolean performLike) {
        InterfaceAction.startLikeActionButtonAnimation(this, likeActionButton, performLike);

        likeCount = prismPost.getLikes() + (performLike ?  1 : -1);
        prismPost.setLikes(likeCount);
        likesCountTextView.setText(String.valueOf(likeCount));
    }

    /**
     *
     */
    private void setupRepostActionButton() {
        InterfaceAction.setupRepostActionButton(this, repostActionButton, isPostReposted);
        repostActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean performRepost = !CurrentUser.hasReposted(prismPost);
                if (performRepost) {
                    AlertDialog repostConfirmationAlertDialog = InterfaceAction.createRepostConfirmationAlertDialog(PrismPostDetailActivity.this, prismPost, repostActionButton, repostCountTextView);
                    repostConfirmationAlertDialog.show();
                } else {
                    handleRepostButtonClick(false);
                }
            }
        });

        repostCountTextView.setText(String.valueOf(repostCount));
        repostCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userLikesIntent = new Intent(PrismPostDetailActivity.this, DisplayUsersActivity.class);
                userLikesIntent.putExtra("UsersInt", 1);
                userLikesIntent.putExtra("UsersDataId", prismPost.getPostId());
                startActivity(userLikesIntent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
    }

    /**
     * Check reposted_posts_map HashMap if it contains the postId or not. If it contains
     * the postId, then firebaseUser has already reposted the post and perform UNREPOST operation
     * If it doesn't exist, firebaseUser has not reposted it yet, and perform REPOST operation
     * Operation REPOST (performRepost = true): does 3 things. First it adds the the firebaseUser's
     * uid to the REPOSTED_USERS table under the post. Then it adds the postId to the
     * USER_REPOSTS table under the firebaseUser. Then it adds the postId and timestamp to the
     * local reposted_posts_map HashMap so that recycler view can update
     * Operation UNREPOST (performRepost = false): undoes above 3 things
     * TODO: update comments
     * @param performRepost
     */
    private void handleRepostButtonClick(boolean performRepost) {
        performUIActivitiesForRepost(performRepost);

        if (performRepost) {
            DatabaseAction.performRepost(prismPost);
        } else {
            DatabaseAction.performUnrepost(prismPost);
        }
    }

    private void performUIActivitiesForRepost(boolean performRepost) {
        InterfaceAction.startRepostActionButtonAnimation(this, repostActionButton, performRepost);

        repostCount = prismPost.getReposts() + (performRepost ? 1 : -1);
        prismPost.setReposts(repostCount);
        repostCountTextView.setText(String.valueOf(repostCount));
    }

    //TODO: Handle other UI for deleting a post
    private void handleDeletionOfPost() {
//        if (getItemCount() == 0) {
//            RelativeLayout noMainPostsRelativeLayout = ((Activity) context)
//                    .findViewById(R.id.no_main_posts_relative_layout);
//            noMainPostsRelativeLayout.setVisibility(View.VISIBLE);
//        }
    }

    /**
     *
     */
    private void setupCollapseUpButton() {
        collapsingToolbarCollapseUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideCollapseUpButton(0);
                appBarLayout.setExpanded(false);
            }
        });
    }

    /**
     *
     */
    private void animateCollapseUpButton() {
        collapsingToolbarCollapseUpButton.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        collapsingToolbarCollapseUpButton.animate()
                                .scaleX(1)
                                .scaleY(1)
                                .setDuration(800)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        animateCollapseUpButton();
                                    }
                                }).start();
                    }
                });
    }

    /**
     *
     */
    private void showCollapseUpButton(int millis) {
        collapseUpButtonHandler.removeCallbacks(showCollapseUpButtonRunnable);
        showCollapseUpButtonRunnable = new Runnable() {
            public void run() {
                collapsingToolbarCollapseUpButton.setVisibility(View.VISIBLE);
                collapsingToolbarCollapseUpButton.animate()
                        .alpha(0.7f)
                        .setDuration(250)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                animateCollapseUpButton();
                            }
                }).start();
            }
        };
        collapseUpButtonHandler.postDelayed(showCollapseUpButtonRunnable, millis);
    }

    /**
     *
     */
    private void hideCollapseUpButton(int millis) {
        collapseUpButtonHandler.removeCallbacks(hideCollapseUpButtonRunnable);
        hideCollapseUpButtonRunnable = new Runnable() {
            public void run() {
                collapsingToolbarCollapseUpButton.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                collapsingToolbarCollapseUpButton.setVisibility(View.GONE);
                            }
                        }).start();
            }
        };
        collapseUpButtonHandler.postDelayed(hideCollapseUpButtonRunnable, millis);
    }

    /**
     *
     */
    private void setupDragArrow() {
        CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) collapsingToolbarDragArrow.getLayoutParams();;
        params.setMargins(0, getStatusBarHeight() + params.topMargin, 0, 0);
        collapsingToolbarDragArrow.setLayoutParams(params);
    }

    /**
     *
     */
    private void animateDragArrow() {
        collapsingToolbarDragArrow.animate()
                .translationYBy(200)
                .scaleX(0.3f)
                .scaleY(0.3f)
                .alpha(0f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        collapsingToolbarDragArrow.animate()
                                .translationYBy(-200)
                                .scaleX(1f)
                                .scaleY(1f)
                                .alpha(0.7f)
                                .setDuration(0)
                                .setInterpolator(new AccelerateDecelerateInterpolator())
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        animateDragArrow();
                                    }
                                });
                    }
                }).start();
    }

    /**
     *
     */
    private void showDragArrow(int millis) {
        dragArrowHandler.removeCallbacks(showDragArrowRunnable);
        showDragArrowRunnable = new Runnable() {
            public void run() {
                collapsingToolbarDragArrow.setVisibility(View.VISIBLE);
                collapsingToolbarDragArrow.animate()
                        .alpha(0.7f)
                        .setDuration(250)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                animateDragArrow();
                            }
                        }).start();
            }
        };
        dragArrowHandler.postDelayed(showDragArrowRunnable, millis);
    }

    /**
     *
     */
    private void hideDragArrow(int millis) {
        dragArrowHandler.removeCallbacks(hideDragArrowRunnable);
        hideDragArrowRunnable = new Runnable() {
            public void run() {
                collapsingToolbarDragArrow.animate()
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                collapsingToolbarDragArrow.setVisibility(View.GONE);
                            }
                        }).start();
            }
        };
        dragArrowHandler.postDelayed(hideDragArrowRunnable, millis);
    }

    /**
     * Three action buttons are shown for each PrismPost
     * Like button likes the PrismPost
     * Repost button reposts the PrismPost to the users profile
     * More button offers a few options
     */
    private void setupActionButtons() {
        setupLikeActionButton();
        setupRepostActionButton();
        setupCollapseUpButton();
        setupDragArrow();
    }

    /**
     * Setup all UI elements
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setupUIElements() {
        setupAppBarLayout();
        setupToolbar();
        setupStatusBar();

        // Setup Typefaces for all text based UI elements
        likesCountTextView.setTypeface(sourceSansProLight);
        repostCountTextView.setTypeface(sourceSansProLight);
        detailUsernameTextView.setTypeface(sourceSansProBold);
        detailPrismPostDateTextView.setTypeface(sourceSansProLight);
        detailPrismPostDescriptionTextView.setTypeface(sourceSansProLight);
        detailPrismPostTagsTextView.setTypeface(sourceSansProLight);

        setupToolbarPullDownLayout();
        setupPrismPostUserInfo();
        setupPrismPostImageView();
        setupActionButtons();
    }

    /**
     * Shortcut for toasting a message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
