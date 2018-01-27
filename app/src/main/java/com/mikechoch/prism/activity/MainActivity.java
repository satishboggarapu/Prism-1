package com.mikechoch.prism.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import java.util.Calendar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikechoch.prism.CurrentUser;
import com.mikechoch.prism.Default;
import com.mikechoch.prism.Key;
import com.mikechoch.prism.R;
import com.mikechoch.prism.ViewPagerAdapter;
import com.mikechoch.prism.Wallpaper;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends FragmentActivity {

    /*
     * Global variables
     */
    private FloatingActionButton uploadImageFab;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private DatabaseReference userReference;

    private Uri uploadedImageUri;
    private String uploadedImageDescription;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MaterialProgressBar fabProgressCircle;
    private CoordinatorLayout mainCoordinateLayout;

//    private Toolbar toolbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // returning false disables menu
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                break;
            case R.id.action_logout:
                auth.signOut();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_layout);
        new CurrentUser(); // generates current user's details
//        toolbar = findViewById(R.id.toolbar);
//        TextView toolbarTextView = findViewById(R.id.toolbar_text_view);
//        toolbarTextView.setTypeface(sourceSansProBold);
//        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        storageReference = Default.STORAGE_REFERENCE;
        databaseReference = Default.ALL_POSTS_REFERENCE;
        userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

        mainCoordinateLayout = findViewById(R.id.main_content);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(5);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_image_filter_hdr_white_36dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_flash_white_36dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_magnify_white_36dp);
        tabLayout.getTabAt(3).setIcon(R.drawable.ic_bell_white_36dp);
        tabLayout.getTabAt(4).setIcon(R.drawable.ic_account_white_36dp);
        tabLayout.getTabAt(0).getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
//                AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
//                params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL |
//                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS |
//                        AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
                tab.getIcon().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);
                viewPager.setCurrentItem(tab.getPosition(), true);
                if (tab.getPosition() == 0 || tab.getPosition() == 1) {
//                    toolbar.setLayoutParams(params);
                    if (!uploadImageFab.isShown()) {
                        Animation scaleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(200);
                        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                uploadImageFab.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        uploadImageFab.startAnimation(scaleAnimation);
                    }
                } else {
//                    params.setScrollFlags(0);
//                    toolbar.setLayoutParams(params);
                    if (uploadImageFab.isShown()) {
                        Animation scaleAnimation = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation.setDuration(200);
                        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                uploadImageFab.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        uploadImageFab.startAnimation(scaleAnimation);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int tabPosition = tab.getPosition();
                switch (tabPosition) {
                    case 0:
                        RecyclerView mainContentRecyclerView = MainActivity.this.findViewById(R.id.main_content_recycler_view);
                        if (mainContentRecyclerView != null) {
                            mainContentRecyclerView.smoothScrollToPosition(0);
                        }
                        break;
                    case 1:
                        RecyclerView trendingContentRecyclerView = MainActivity.this.findViewById(R.id.trending_content_recycler_view);
                        if (trendingContentRecyclerView != null) {
                            trendingContentRecyclerView.smoothScrollToPosition(0);
                        }
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    default:
                        break;
                }
            }
        });

        // Initialize uploadImageFab and OnClickListener to take you to ImageUploadActivity
        fabProgressCircle = findViewById(R.id.fabProgressCircle);
        uploadImageFab = findViewById(R.id.upload_image_fab);
        uploadImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageUploadIntent = new Intent( MainActivity.this, ImageUploadActivity.class);
                startActivityForResult(imageUploadIntent, Default.IMAGE_UPLOAD_INTENT_REQUEST_CODE);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        // Ask user for write permissions to external storage
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Default.MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }
    }

    /**
     * TODO populate card views with images from listOfImages
     */
    private void refreshPageWithImages() {
//        System.out.println(listOfImages);
    }

    @SuppressLint("SimpleDateFormat")
    private void uploadImageToCloud() {
        StorageReference filePath = storageReference.child(Key.STORAGE_IMAGE_REF).child(uploadedImageUri.getLastPathSegment());
        filePath.putFile(uploadedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                DatabaseReference reference = databaseReference.push();

                String imageUri = downloadUrl.toString();
                String description = uploadedImageDescription;
                String username = auth.getCurrentUser().getDisplayName();
                String userId = auth.getCurrentUser().getUid();
                Long timestamp = -1 * Calendar.getInstance().getTimeInMillis();
                int likes = 0;
                String postId = reference.getKey();

                DatabaseReference userPostRef = userReference.child(Key.DB_REF_USER_UPLOADS).child(postId);
                userPostRef.setValue(timestamp);

                Wallpaper wallpaper = new Wallpaper(imageUri, description, username, userId, timestamp, likes, postId);

                reference.setValue(wallpaper).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        snackTime("Successfully uploaded image");
                        fabProgressCircle.setVisibility(View.GONE);
                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                new Thread(new Runnable() {
                    @SuppressLint("NewApi")
                    @Override
                    public void run() {
                        int progress = (int) ((taskSnapshot.getBytesTransferred() * 100) / taskSnapshot.getTotalByteCount());
                        int currentProgress = fabProgressCircle.getProgress();
                        while (currentProgress <= progress) {
                            try {
                                Thread.sleep(40);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            fabProgressCircle.setProgress(currentProgress++, Build.VERSION.SDK_INT >= Build.VERSION_CODES.N);
                        }
                    }
                }).start();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                snackTime("Failed to upload image");
                fabProgressCircle.setVisibility(View.GONE);
                e.printStackTrace();
            }
        });
    }

    /**
     *
     */
    private class ImageUploadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... params) {
            uploadImageToCloud();
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
        }
    }

    /**
     *
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case Default.IMAGE_UPLOAD_INTENT_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    fabProgressCircle.setProgress(0);
                    fabProgressCircle.setVisibility(View.VISIBLE);
                    uploadedImageUri = Uri.parse(data.getStringExtra("ImageUri"));
                    uploadedImageDescription = data.getStringExtra("ImageDescription");
                    new ImageUploadTask().execute();
                }
                break;
            default:
                break;
        }
    }

    /**
     *
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
     * Shortcut for displaying a Toast message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shortcut for displaying a Snackbar message
     */
    private void snackTime(String message) {
        Snackbar.make(mainCoordinateLayout, message, Toast.LENGTH_SHORT).show();
    }
}
