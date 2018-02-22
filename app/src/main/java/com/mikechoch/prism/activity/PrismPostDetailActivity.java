package com.mikechoch.prism.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.mikechoch.prism.R;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.helper.Helper;

import ooo.oxo.library.widget.PullBackLayout;


/**
 * Created by mikechoch on 2/19/18.
 */

public class PrismPostDetailActivity extends AppCompatActivity implements PullBackLayout.Callback {

    /*
     * Globals
     */
    private int scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL|AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED;
    private int noScrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL|AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;

    private PullBackLayout prismPostDetailPuller;
    private AppBarLayout appBarLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;

    private RelativeLayout prismPostDetailsRelativeLayout;
    private ImageView likeActionButton;
    private TextView likeCountTextView;
    private ImageView repostActionButton;
    private TextView repostCountTextView;
    private ImageView moreActionButton;
    private ImageView detailImageView;
    private ImageView detailUserProfilePictureImageView;
    private TextView detailUsernameTextView;
    private TextView detailPrismPostDateTextView;
    private TextView detailPrismPostDescriptionTextView;
    private TextView detailPrismPostTagsTextView;

    private PrismPost prismPost;


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
                super.onBackPressed();
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
        prismPostDetailPuller = findViewById(R.id.prism_post_detail_puller);
        appBarLayout = findViewById(R.id.prism_post_detail_app_bar_layout);
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        prismPostDetailsRelativeLayout = findViewById(R.id.prism_post_detail_relative_layout);
        likeActionButton = findViewById(R.id.image_like_button);
        likeCountTextView = findViewById(R.id.like_count);
        repostActionButton = findViewById(R.id.image_repost_button);
        repostCountTextView = findViewById(R.id.repost_count);
        moreActionButton = findViewById(R.id.image_more_button);
        detailImageView = findViewById(R.id.prism_post_detail_image_view);
        detailUserProfilePictureImageView = findViewById(R.id.prism_post_detail_user_profile_picture_image_view);
        detailUsernameTextView = findViewById(R.id.prism_post_detail_username_text_view);
        detailPrismPostDateTextView = findViewById(R.id.prism_post_detail_date_text_view);
        detailPrismPostDescriptionTextView = findViewById(R.id.prism_post_description);
        detailPrismPostTagsTextView = findViewById(R.id.prism_post_tags);

        Bundle extras = getIntent().getExtras();
        prismPost = extras.getParcelable("PrismPostDetail");

        System.out.println(prismPost.getPostId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = extras.getString("PrismPostDetailTransitionName");
            detailImageView.setTransitionName(imageTransitionName);
        }

        setupUIElements();
        prismPostDetailPuller.setCallback(this);
    }

    @Override
    public void onPullStart() {

    }

    @Override
    public void onPull(float pullFloat) {
        float viewAlpha = Math.abs(1 - pullFloat);
        prismPostDetailPuller.setAlpha(viewAlpha);
    }

    @Override
    public void onPullCancel() {
        prismPostDetailPuller.setAlpha(1f);
    }

    @Override
    public void onPullComplete() {
        supportFinishAfterTransition();
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
    private void setupStatusBar() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Setup the toolbar and back button to return to MainActivity
     */
    private void setupToolbar() {
        toolbar.setTitle("");

        CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        toolbar.setLayoutParams(params);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
                        // Calculate the PrismPost info window height
                        int userInfoHeight = detailUserProfilePictureImageView.getHeight() +
                                detailPrismPostDescriptionTextView.getHeight() +
                                detailPrismPostTagsTextView.getHeight() +
                                prismPostDetailsRelativeLayout.getPaddingBottom();

                        // Set the height of the appBarLayout, detailImageView, and toolbar
                        // This ensures proper UI response when scrolling the image and info window
                        appBarLayout.getLayoutParams().height = resource.getHeight();
                        detailImageView.getLayoutParams().height = resource.getHeight();
                        toolbar.getLayoutParams().height = screenHeight - getBottomNavigationBarHeight() - userInfoHeight;

                        // Check that the image height is larger or equal to actual screen height
                        // If so, set ScaleType to CENTER_CROP
                        // Otherwise, set ScaleType to FIT_START
                        boolean isLongPortraitImage = resource.getHeight() >= screenHeight;
                        detailImageView.setScaleType(isLongPortraitImage ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_START);

                        // Check that the image and info window height is larger or equal to actual screen height
                        // If so, enable collapsing toolbar using scroll flags
                        // Otherwise, disable collapsing toolbar using scroll flags
                        boolean isScrollImage = (resource.getHeight() + userInfoHeight) >= screenHeight;

                        // Set scroll flags for collapsingToolbarLayout containing the PrismPost image
                        AppBarLayout.LayoutParams collapsingToolbarLayoutLayoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
                        collapsingToolbarLayoutLayoutParams.setScrollFlags(isScrollImage ? scrollFlags : noScrollFlags);
                        collapsingToolbarLayout.setLayoutParams(collapsingToolbarLayoutLayoutParams);

                        startPostponedEnterTransition();
                        return false;
                    }
                })
                .into(detailImageView);
    }

    /**
     *
     */
    private void setupPrismPostUserInfo() {
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
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        setupPrismPostImageView();
        setupStatusBar();
        setupToolbar();

        // Setup Typefaces for all text based UI elements
        likeCountTextView.setTypeface(sourceSansProLight);
        repostCountTextView.setTypeface(sourceSansProLight);
        detailUsernameTextView.setTypeface(sourceSansProBold);
        detailPrismPostDateTextView.setTypeface(sourceSansProLight);
        detailPrismPostDescriptionTextView.setTypeface(sourceSansProLight);
        detailPrismPostTagsTextView.setTypeface(sourceSansProLight);

        detailPrismPostTagsTextView.setText(Html.fromHtml(
                "<u>#burger</u> " +
                        "<u>#delicous</u> " +
                        "<u>#foodporn</u> " +
                        "<u>#inandout</u> " +
                        "<u>#fries</u> " +
                        "<u>#carkeys</u> " +
                        "<u>#amazing</u>"));

        setupPrismPostUserInfo();

    }
}
