package com.mikechoch.prism.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.mikechoch.prism.R;
import com.mikechoch.prism.attribute.PrismUser;

import java.text.DecimalFormat;

/**
 * Created by mikechoch on 3/10/18.
 */

public class ShowUserProfilePictureActivity extends AppCompatActivity {

    /*
    * Globals
    */
    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;

    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    private RelativeLayout dismissClickRelativeLayout;
    private LinearLayout userProfilePictureLinearLayout;
    private ImageView largeUserProfilePictureImageView;

    private PrismUser prismUser;

    private ScaleGestureDetector mScaleDetector;
    private float startDistanceChange;
    private float totalDistanceChange;
    private boolean isZooming = false;


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
        setContentView(R.layout.show_user_profile_picture_activity_layout);

        // Get the screen density of the current phone for later UI element scaling
        scale = getResources().getDisplayMetrics().density;

        // Get the screen width and height of the current phone
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        // Create two typefaces
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Initialize all UI elements
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.app_bar_layout);

        dismissClickRelativeLayout = findViewById(R.id.show_user_profile_picture_coordinate_layout);
        userProfilePictureLinearLayout = findViewById(R.id.user_profile_picture_linear_layout);
        largeUserProfilePictureImageView = findViewById(R.id.large_user_profile_picture_image_view);

        prismUser = getIntent().getParcelableExtra("PrismUser");

        mScaleDetector = new ScaleGestureDetector(this, new ShowUserProfilePictureActivity.MyPinchListener());

        setupUIElements();
    }

    /**
     * Setup the toolbar
     */
    private void setupToolbar() {
        toolbar.setTitle("");

        setSupportActionBar(toolbar);
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
     *
     */
    private void setupDismissClickCoordinateLayout() {
        dismissClickRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toast("Clicked");
                ShowUserProfilePictureActivity.super.onBackPressed();
            }
        });
    }

    /**
     *
     */
    private void setupUserProfilePicture() {
        supportStartPostponedEnterTransition();

        Glide.with(this)
                .asBitmap()
                .load(prismUser.getProfilePicture().hiResUri)
                .apply(new RequestOptions().fitCenter().override((int) (screenWidth * 0.8)))
                .into(new BitmapImageViewTarget(largeUserProfilePictureImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        if (!prismUser.getProfilePicture().isDefault) {
                            int whiteOutlinePadding = (int) (5 * scale);
                            largeUserProfilePictureImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                            largeUserProfilePictureImageView.setBackground(getResources().getDrawable(R.drawable.circle_profile_frame));
                        } else {
                            largeUserProfilePictureImageView.setPadding(0, 0, 0, 0);
                            largeUserProfilePictureImageView.setBackground(null);
                        }

                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        drawable.setCircular(true);
                        largeUserProfilePictureImageView.setImageDrawable(drawable);

                        startPostponedEnterTransition();
                    }
                });

        userProfilePictureLinearLayout.getLayoutParams().height = (int) (screenWidth * 0.8);
        userProfilePictureLinearLayout.getLayoutParams().width = (int) (screenWidth * 0.8);
        userProfilePictureLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        System.out.println("DOWN");
                        break;
                    case MotionEvent.ACTION_UP:
//                        System.out.println("UP");
                        isZooming = false;
                        largeUserProfilePictureImageView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150);
                        totalDistanceChange = 0;
                        break;
                }

                if (event.getPointerCount() == 2 && !isZooming) {
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
                    largeUserProfilePictureImageView.setPivotX(pivotPointX);
                    largeUserProfilePictureImageView.setPivotY(pivotPointY);

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
                largeUserProfilePictureImageView.setScaleX(imageScale);
                largeUserProfilePictureImageView.setScaleY(imageScale);
            }
            return true;
        }
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        setupToolbar();
        setupStatusBar();

        setupDismissClickCoordinateLayout();
        setupUserProfilePicture();
    }

    /**
     * Shortcut for toasting a message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
