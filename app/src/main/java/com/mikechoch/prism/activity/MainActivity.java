package com.mikechoch.prism.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikechoch.prism.InterfaceAction;
import com.mikechoch.prism.attribute.CurrentUser;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.R;
import com.mikechoch.prism.adapter.MainViewPagerAdapter;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.fragments.MainContentFragment;

import java.util.Calendar;


public class MainActivity extends FragmentActivity {

    /*
     * Globals
     */
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private DatabaseReference userReference;

    private float scale;
    private Animation hideFabAnimation;
    private Animation showFabAnimation;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private AppBarLayout.LayoutParams params;

    private Toolbar toolbar;

    private CoordinatorLayout mainCoordinateLayout;
    private TabLayout prismTabLayout;
    private ViewPager prismViewPager;
    private ImageView imageUploadPreview;
    private TextView uploadingImageTextView;
    private RelativeLayout prismDecorationRelativeLayout;
    private RelativeLayout uploadingImageRelativeLayout;
    private FloatingActionButton uploadImageFab;
    private ProgressBar imageUploadProgressBar;

    private Uri profilePictureUri;
    private Uri uploadedImageUri;
    private String uploadedImageDescription;
    private boolean isUploadingImage = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);

        auth = FirebaseAuth.getInstance();
        storageReference = Default.STORAGE_REFERENCE;
        databaseReference = Default.ALL_POSTS_REFERENCE;
        userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

        // Check if firebaseUser is logged in
        // Otherwise intent to LoginActivity
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            intentToLoginActivity();
        }

        // Ask firebaseUser for write permissions to external storage
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Default.MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }

        // Get the screen density of the current phone for later UI element scaling
        scale = getResources().getDisplayMetrics().density;

        // Create uploadImageFab showing and hiding animations
        showFabAnimation = createFabShowAnimation(false);
        hideFabAnimation = createFabShowAnimation(true);

        // Initialize normal and bold Prism font
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        toolbar = findViewById(R.id.toolbar);
        TextView toolbarTextView = findViewById(R.id.prism_toolbar_title);
        toolbarTextView.setTypeface(sourceSansProBold);

        params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();

        // Initialize all UI elements
        mainCoordinateLayout = findViewById(R.id.main_coordinate_layout);
        prismTabLayout = findViewById(R.id.prism_tab_layout);
        prismViewPager = findViewById(R.id.prism_view_pager);
        imageUploadPreview = findViewById(R.id.image_upload_preview);
        uploadingImageTextView = findViewById(R.id.uploading_image_text_view);
        prismDecorationRelativeLayout = findViewById(R.id.prism_toolbar_decoration);
        uploadingImageRelativeLayout = findViewById(R.id.uploading_image_relative_layout);
        uploadImageFab = findViewById(R.id.upload_image_fab);
        imageUploadProgressBar = findViewById(R.id.image_upload_progress_bar);

        setupUIElements();

        // Generates current firebaseUser's details
        new CurrentUser(this);
        new InterfaceAction(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * When a permission is allowed, this function will run and you can
     * Check for this allow and do something
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Default.MY_PERMISSIONS_REQUEST_READ_MEDIA:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Code here for allowing write permission
                }
                break;
            default:
                break;
        }
    }

    /**
     * Called when an activity is intent with startActivityForResult and the result is intent back
     * This allows you to check the requestCode that came back and do something
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            // If requestCode is for ImageUploadActivity
            case Default.IMAGE_UPLOAD_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    uploadingImageTextView.setText("Uploading image...");
                    imageUploadProgressBar.setProgress(0);
                    imageUploadProgressBar.setIndeterminate(false);
                    uploadingImageRelativeLayout.setVisibility(View.VISIBLE);
                    prismDecorationRelativeLayout.setVisibility(View.GONE);

                    params.setScrollFlags(0);
                    toolbar.setLayoutParams(params);

                    uploadedImageUri = Uri.parse(data.getStringExtra("ImageUri"));
                    uploadedImageDescription = data.getStringExtra("ImageDescription");

                    Glide.with(this)
                            .asBitmap()
                            .thumbnail(0.05f)
                            .load(uploadedImageUri)
                            .apply(new RequestOptions().centerCrop())
                            .into(new BitmapImageViewTarget(imageUploadPreview) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                                    drawable.setCircular(true);
                                    imageUploadPreview.setImageDrawable(drawable);
                                }
                            });
                    uploadImageToCloud();
                }
                break;
            // If requestCode is for ProfilePictureUploadActivity
            case Default.PROFILE_PIC_UPLOAD_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    profilePictureUri = Uri.parse(data.getStringExtra("CroppedProfilePicture"));

                    ImageView profilePictureImageView = findViewById(R.id.user_profile_profile_picture_image_view);
                    Glide.with(this)
                            .asBitmap()
                            .thumbnail(0.05f)
                            .load(profilePictureUri)
                            .apply(new RequestOptions().fitCenter())
                            .into(new BitmapImageViewTarget(profilePictureImageView) {
                                @Override
                                protected void setResource(Bitmap resource) {
                                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                                    drawable.setCircular(true);
                                    profilePictureImageView.setImageDrawable(drawable);

                                    int whiteOutlinePadding = (int) (2 * scale);
                                    profilePictureImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                                    profilePictureImageView.setBackground(getResources().getDrawable(R.drawable.circle_profile_frame));
                                }
                            });

                    uploadProfilePictureToCloud();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Give PageChangeListener control to TabLayout
     * Create MainViewPagerAdapter and set it for the ViewPager
     */
    private void setupPrismViewPager() {
        prismViewPager.setOffscreenPageLimit(Default.MAIN_VIEW_PAGER_SIZE);
        prismViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(prismTabLayout));
        MainViewPagerAdapter prismViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager());
        prismViewPager.setAdapter(prismViewPagerAdapter);
        prismTabLayout.setupWithViewPager(prismViewPager);
    }

    /**
     * Setup for the TabLayout
     * Give each tab an icon and set the listener for selecting, reselecting, and unselecting
     * Selected tabs will be a ColorAccent and unselected tabs White
     */
    private void setupPrismTabLayout() {
        // Setup all TabLayout tab icons
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_HOME).setIcon(R.drawable.ic_image_filter_hdr_white_36dp);
//        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_TRENDING).setIcon(R.drawable.ic_flash_white_36dp);
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_SEARCH - 1).setIcon(R.drawable.ic_magnify_white_36dp);
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_NOTIFICATIONS - 1).setIcon(R.drawable.ic_bell_white_36dp);
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_PROFILE - 1).setIcon(R.drawable.ic_menu_white_36dp);

        // Create the selected and unselected tab icon colors
        int tabUnselectedColor = Color.WHITE;
        int tabSelectedColor = getResources().getColor(R.color.colorAccent);

        // Make first tab selected color and all others unselected
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_HOME).getIcon().setColorFilter(
                tabSelectedColor, PorterDuff.Mode.SRC_IN);
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_SEARCH - 1).getIcon().setColorFilter(
                tabUnselectedColor, PorterDuff.Mode.SRC_IN);
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_NOTIFICATIONS - 1).getIcon().setColorFilter(
                tabUnselectedColor, PorterDuff.Mode.SRC_IN);
        prismTabLayout.getTabAt(Default.MAIN_VIEW_PAGER_PROFILE - 1).getIcon().setColorFilter(
                tabUnselectedColor, PorterDuff.Mode.SRC_IN);

        // Setup the tab selected, unselected, and reselected listener
        prismTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                params.setScrollFlags(isUploadingImage ?
//                        0 : AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
//                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
//                        AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                // Sets the selected tab to the selected color and
                // If at the HOME tab the uploadImageFab will be shown
                // Otherwise, the uploadImageFab will be hidden
                tab.getIcon().setColorFilter(tabSelectedColor, PorterDuff.Mode.SRC_IN);
                prismViewPager.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() <= Default.MAIN_VIEW_PAGER_TRENDING - 1 && !uploadImageFab.isShown()) {
//                    toolbar.setLayoutParams(params);
                    uploadImageFab.startAnimation(showFabAnimation);
                } else if (tab.getPosition() > Default.MAIN_VIEW_PAGER_TRENDING - 1 && uploadImageFab.isShown()) {
//                    params.setScrollFlags(0);
//                    toolbar.setLayoutParams(params);
                    uploadImageFab.startAnimation(hideFabAnimation);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Set the tab unselected to the unselected color
                tab.getIcon().setColorFilter(tabUnselectedColor, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Switch statement handing reselected tabs
                int tabPosition = tab.getPosition();
                switch (tabPosition) {
                    // HOME tab will bring the user back to the top of the mainContentRecyclerView
                    case 0:
                        RecyclerView mainContentRecyclerView = MainActivity.this
                                .findViewById(R.id.main_content_recycler_view);
                        if (mainContentRecyclerView != null) {
                            LinearLayoutManager layoutManager  = (LinearLayoutManager)
                                    mainContentRecyclerView.getLayoutManager();
                            if (layoutManager.findFirstVisibleItemPosition() < 10) {
                                mainContentRecyclerView.smoothScrollToPosition(0);
                            } else {
                                mainContentRecyclerView.scrollToPosition(0);
                            }
                        }
                        break;

                    // SEARCH tab will...
                    case 1:
                        break;

                    // NOTIFICATIONS tab will...
                    case 2:
                        break;

                    // PROFILE tab will bring the user back to the top of the profileScrollView
                    case 3:
//                        ScrollView profileScrollView = MainActivity.this.findViewById(R.id.profile_scroll_view);
//                        RecyclerView profilePostsRecyclerView = MainActivity.this.findViewById(R.id.user_posts_recycler_view);
//                        StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) profilePostsRecyclerView.getLayoutManager();
//                        if (profileScrollView != null && profilePostsRecyclerView != null) {
//                            int[] positions = new int[staggeredGridLayoutManager.getSpanCount()];
//                            staggeredGridLayoutManager.findFirstVisibleItemPositions(positions);
//                            if (positions[0] < 10) {
//                                profileScrollView.smoothScrollTo(0, 0);
//                            } else {
//                                profileScrollView.scrollTo(0, 0);
//                            }
//                        }
                        break;

                    case 4:
                        break;

                    default:
                        break;
                }
            }
        });
    }

    /**
     * Setup the UploadImageFab, so when it is clicked it will Intent to UploadImageActivity
     */
    private void setupUploadImageFab() {
        uploadImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intentToUploadImageActivity();
            }
        });
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        // Setup Typefaces for all text based UI elements
        uploadingImageTextView.setTypeface(sourceSansProLight);

        setupPrismViewPager();
        setupPrismTabLayout();
        setupUploadImageFab();

        TabLayout.Tab currentTab = prismTabLayout.getTabAt(prismViewPager.getCurrentItem());
        currentTab.select();

    }

    /**
     * Intent to Login Activity from Main Activity
     */
    private void intentToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Intent to Upload Image Activity from Main Activity
     */
    private void intentToUploadImageActivity() {
        Intent imageUploadIntent = new Intent( MainActivity.this, ImageUploadActivity.class);
        startActivityForResult(imageUploadIntent, Default.IMAGE_UPLOAD_INTENT_REQUEST_CODE);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Takes the profilePicUriString and stores the image to cloud. Once the image file is
     * successfully uploaded to cloud successfully, it adds the profilePicUriString to
     * the firebaseUser's profile details section
     */
    private void uploadProfilePictureToCloud() {
        StorageReference profilePicRef = storageReference.child(Key.STORAGE_USER_PROFILE_IMAGE_REF).child(profilePictureUri.getLastPathSegment());
        profilePicRef.putFile(profilePictureUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult().getDownloadUrl();
                    DatabaseReference userRef = userReference.child(Key.USER_PROFILE_PIC);
                    userRef.setValue(downloadUrl.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Log.wtf(Default.TAG_DB, Message.PROFILE_PIC_UPDATE_FAIL, task.getException());
                            }
                        }
                    });
                } else {
                    Log.e(Default.TAG_DB, Message.FILE_UPLOAD_FAIL, task.getException());
                    toast("Unable to update profile picture");
                }
            }
        });
    }

    /**
     * Takes in a boolean shouldHide and will create a hiding and showing animation
     */
    private Animation createFabShowAnimation(boolean shouldHide) {
        float scaleFromXY = shouldHide ? 1f : 0f;
        float scaleToXY = shouldHide ? 0f : 1f;
        float pivotXY = 0.5f;
        Animation scaleAnimation  = new ScaleAnimation(scaleFromXY, scaleToXY, scaleFromXY, scaleToXY,
                Animation.RELATIVE_TO_SELF, pivotXY,
                Animation.RELATIVE_TO_SELF, pivotXY);
        scaleAnimation.setDuration(200);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                uploadImageFab.setVisibility(shouldHide ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        return scaleAnimation;
    }

    /**
     *  Takes the uploadedImageUri (which is the image that firebaseUser chooses from local storage)
     *  and uploads the file to cloud. Once that is successful, the a new post is created in
     *  ALL_POSTS section and the post details are pushed. Then the postId is added to the
     *  USER_UPLOADS section for the current firebaseUser
     */
    @SuppressLint("SimpleDateFormat")
    private void uploadImageToCloud() {
        StorageReference postImageRef = storageReference.child(Key.STORAGE_POST_IMAGES_REF).child(uploadedImageUri.getLastPathSegment());
        postImageRef.putFile(uploadedImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Uri downloadUrl = task.getResult().getDownloadUrl();
                    DatabaseReference postReference = databaseReference.push();
                    PrismPost prismPost = createPrismPostObjectForUpload(downloadUrl);

                    // Add postId to USER_UPLOADS table
                    DatabaseReference userPostRef = userReference.child(Key.DB_REF_USER_UPLOADS).child(postReference.getKey());
                    userPostRef.setValue(prismPost.getTimestamp());

                    // Create the post in cloud and on success, add the image to local recycler view adapter
                    postReference.setValue(prismPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateLocalRecyclerViewWithNewPost(prismPost);
                            } else {
                                uploadingImageTextView.setText("Failed to make the post");
                                Log.wtf(Default.TAG_DB, Message.POST_UPLOAD_FAIL, task.getException());
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                imageUploadProgressBar.setProgress(100, true);
                            } else {
                                imageUploadProgressBar.setProgress(100);
                            }
                        }
                    });
                } else {
                    Log.e(Default.TAG_DB, Message.FILE_UPLOAD_FAIL, task.getException());
                    toast("Failed to upload the image to cloud");
                }


            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int progress = (int) ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            imageUploadProgressBar.setProgress(progress, true);
                        } else {
                            imageUploadProgressBar.setProgress(progress);
                        }
                    }
                });
            }
        });
    }

    /**
     * Takes new prismPost object that got uploaded to cloud and adds it to the recyclerViewAdapter
     * and wraps up other UI elements such as textviews and progress spinners
     * @param prismPost
     */
    private void updateLocalRecyclerViewWithNewPost(PrismPost prismPost) {
        uploadingImageTextView.setText("Finishing up...");
        prismPost.setPrismUser(CurrentUser.prismUser);
        RecyclerView mainContentRecyclerView = MainActivity.this.findViewById(R.id.main_content_recycler_view);
        LinearLayoutManager layoutManager  = (LinearLayoutManager) mainContentRecyclerView.getLayoutManager();
        RelativeLayout noMainPostsRelativeLayout = MainActivity.this.findViewById(R.id.no_main_posts_relative_layout);
        MainContentFragment.prismPostArrayList.add(0, prismPost);
        mainContentRecyclerView.getAdapter().notifyItemInserted(0);
        noMainPostsRelativeLayout.setVisibility(View.GONE);

        if (layoutManager.findFirstVisibleItemPosition() < 10) {
            mainContentRecyclerView.smoothScrollToPosition(0);
        } else {
            mainContentRecyclerView.scrollToPosition(0);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadingImageTextView.setText("Done");
            }
        }, 1000);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                uploadingImageRelativeLayout.setVisibility(View.GONE);
                prismDecorationRelativeLayout.setVisibility(View.VISIBLE);

                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
                        AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                toolbar.setLayoutParams(params);
            }
        }, 2000);
    }

    /**
     * Takes in the downloadUri that was create in cloud and reference to the post that
     * got created in cloud and prepares the PrismPost object that will be pushed
     */
    private PrismPost createPrismPostObjectForUpload(Uri downloadUrl) {
        String imageUri = downloadUrl.toString();
        String description = uploadedImageDescription;
        String userId = auth.getCurrentUser().getUid();
        Long timestamp = -1 * Calendar.getInstance().getTimeInMillis();
        return new PrismPost(imageUri, description, userId, timestamp);
    }


    /**
     * Shortcut for displaying a Toast message
     */
    private void toast(String bread) {
        Toast.makeText(this, bread, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shortcut for displaying a Snackbar message
     */
    private void snackTime(String message) {
        Snackbar.make(mainCoordinateLayout, message, Toast.LENGTH_SHORT).show();
    }
}
