package com.mikechoch.prism.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.mikechoch.prism.CurrentUser;
import com.mikechoch.prism.Key;
import com.mikechoch.prism.R;
import com.mikechoch.prism.ViewPagerAdapter;
import com.mikechoch.prism.Wallpaper;
import com.mikechoch.prism.fragments.MainContentFragment;

public class MainActivity extends FragmentActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 1;

    private FloatingActionButton uploadImageFab;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    private TabLayout tabLayout;
    private ViewPager viewPager;

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
        uploadImageFab = findViewById(R.id.upload_image_fab);
        uploadImageFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent imageUploadIntent = new Intent( MainActivity.this, ImageUploadActivity.class);
                startActivity(imageUploadIntent);
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        }
    }

    /**
     * TODO populate card views with images from listOfImages
     */
    private void refreshPageWithImages() {
//        System.out.println(listOfImages);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_MEDIA:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Code here for allowing write permission
                }
                break;
            default:
                break;
        }
    }

    /**
     * Shortcut for toasting a message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
