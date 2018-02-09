package com.mikechoch.prism.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.mikechoch.prism.CurrentUser;
import com.mikechoch.prism.adapter.StaggeredGridRecyclerViewAdapter;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.LoginActivity;
import com.mikechoch.prism.activity.ProfilePictureUploadActivity;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/22/18.
 */

public class ProfileFragment extends Fragment {

    /*
     * Global variables
     */
    private FirebaseAuth auth;
    private DatabaseReference userReference;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private String[] setProfilePicStrings = {"Choose from gallery", "Take a selfie"};

    private ImageView userProfilePicImageView;
    private TextView followersCountTextView;
    private TextView followersLabelTextView;
    private TextView postsCountTextView;
    private TextView postsLabelTextView;
    private TextView followingCountTextView;
    private TextView followingLabelTextView;
    private TextView userUsernameTextView;
    private TextView userFullNameTextView;
    private RecyclerView staggeredGridRecyclerView;
    private Button logoutButton;


    public static final ProfileFragment newInstance(int title, String message) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle(2);
        bundle.putInt("Title", title);
        bundle.putString("Extra_Message", message);
        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int title = getArguments().getInt("Title");
        String message = getArguments().getString("Extra_Message");

        auth = FirebaseAuth.getInstance();
        userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

        scale = this.getResources().getDisplayMetrics().density;

        sourceSansProLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment_layout, container, false);

        // Initialize all UI elements
        userProfilePicImageView = view.findViewById(R.id.profile_frag_profile_picture_image_view);
        followersCountTextView = view.findViewById(R.id.followers_count_text_view);
        followersLabelTextView = view.findViewById(R.id.followers_label_text_view);
        postsCountTextView = view.findViewById(R.id.posts_count_text_view);
        postsLabelTextView = view.findViewById(R.id.posts_label_text_view);
        followingCountTextView = view.findViewById(R.id.following_count_text_view);
        followingLabelTextView = view.findViewById(R.id.following_label_text_view);
        userUsernameTextView = view.findViewById(R.id.profile_frag_username_text_view);
        userFullNameTextView = view.findViewById(R.id.profile_frag_full_name_text_view);
        staggeredGridRecyclerView = view.findViewById(R.id.user_posts_recycler_view);
        logoutButton = view.findViewById(R.id.logout_button);

        logoutButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        logoutButton.setTypeface(sourceSansProLight);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        setupUIElements();

        return view;
    }

    /**
     * Setup the userProfilePicImageView so it is populated with a Default or custom picture
     * When clicked it will show an AlertDialog of options for changing the picture
     */
    private void setupUserProfilePicImageView() {
        if (CurrentUser.profilePicture != null) {
            Glide.with(this)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(CurrentUser.profilePicture.hiResUri)
                    .apply(new RequestOptions().fitCenter())
                    .into(new BitmapImageViewTarget(userProfilePicImageView) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (!CurrentUser.profilePicture.isDefault) {
                                int whiteOutlinePadding = (int) (2 * scale);
                                userProfilePicImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                                userProfilePicImageView.setBackground(getActivity().getResources().getDrawable(R.drawable.circle_profile_frame));
                            } else {
                                userProfilePicImageView.setPadding(0, 0, 0, 0);
                                userProfilePicImageView.setBackground(null);
                            }

                            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                            drawable.setCircular(true);
                            userProfilePicImageView.setImageDrawable(drawable);
                        }
                    });
        }

        userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog setProfilePictureAlertDialog = createSetProfilePictureAlertDialog();
                setProfilePictureAlertDialog.show();
            }
        });
    }

    /**
     * Create an AlertDialog for when the userProfilePicImageView is clicked
     * Gives the option to take a picture or select one from gallery
     */
    private AlertDialog createSetProfilePictureAlertDialog() {
        AlertDialog.Builder profilePictureAlertDialog = new AlertDialog.Builder(getActivity());
        profilePictureAlertDialog.setTitle("Set profile picture");
        profilePictureAlertDialog.setItems(setProfilePicStrings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent imageUploadIntent = new Intent(getActivity(), ProfilePictureUploadActivity.class);
                        getActivity().startActivityForResult(imageUploadIntent, Default.PROFILE_PIC_UPLOAD_INTENT_REQUEST_CODE);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
     * Setup the TextViews on the ProfileFragment
     */
    private void setupUsernameAndFullNameTextView() {
        userUsernameTextView.setText(CurrentUser.username);
        userFullNameTextView.setText(CurrentUser.full_name);
    }

    /**
     * Create a StaggeredGridLayoutManager and give it a spanCount of 2
     * Create a StaggeredGridRecyclerViewAdapter
     * Set the layout manager and adapter of the RecyclerView
     */
    private void setupUserPostsRecyclerView() {
        ArrayList<Drawable> drawableArrayList = new ArrayList<>();
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));
        drawableArrayList.add(getResources().getDrawable(R.mipmap.ic_launcher));

        staggeredGridRecyclerView.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1);
        staggeredGridRecyclerView.setLayoutManager(staggeredGridLayoutManager);
        StaggeredGridRecyclerViewAdapter staggeredGridRecyclerViewAdapter = new StaggeredGridRecyclerViewAdapter(getActivity(), drawableArrayList);
        staggeredGridRecyclerView.setAdapter(staggeredGridRecyclerViewAdapter);
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

        setupUserProfilePicImageView();
        setupUsernameAndFullNameTextView();
        setupUserPostsRecyclerView();
    }

}
