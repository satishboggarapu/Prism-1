package com.mikechoch.prism.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.activity.PrismUserProfileActivity;
import com.mikechoch.prism.fire.CurrentUser;
import com.mikechoch.prism.attribute.PrismUser;
import com.mikechoch.prism.R;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.fire.DatabaseAction;
import com.mikechoch.prism.helper.Helper;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/21/18.
 */

public class DisplayUsersRecyclerViewAdapter extends RecyclerView.Adapter<DisplayUsersRecyclerViewAdapter.ViewHolder> {

    /*
     * Global variables
     */
    private Context context;
    private ArrayList<PrismUser> prismUserArrayList;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;


    public DisplayUsersRecyclerViewAdapter(Context context, ArrayList<PrismUser> prismUserArrayList) {
        this.context = context;
        this.prismUserArrayList = prismUserArrayList;

        // Get the density scale of the current device
        this.scale = context.getResources().getDisplayMetrics().density;

        // Create two typefaces
        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.users_recycler_view_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(prismUserArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return prismUserArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private FirebaseAuth auth;
        private DatabaseReference userReference;

        private PrismUser prismUser;
        private RelativeLayout userRelativeLayout;
        private ImageView userProfilePicture;
        private TextView usernameTextView;
        private TextView userFullNameText;
        private Button userFollowButton;


        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE;

            // Initialize all UI elements
            userRelativeLayout = itemView.findViewById(R.id.display_user_relative_layout);
            userProfilePicture = itemView.findViewById(R.id.user_profile_picture_image_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            userFullNameText = itemView.findViewById(R.id.full_name_text_view);
            userFollowButton = itemView.findViewById(R.id.small_follow_user_button);
        }

        /**
         * Set data for the ViewHolder UI elements
         */
        public void setData(PrismUser prismUser) {
            this.prismUser = prismUser;
            setupUIElements();
        }

        /**
         *
         */
        private void setupUserRelativeLayout() {
            userRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intentToUserProfileActivity();
                }
            });
        }

        /**
         * Intent from the current clicked PrismPost user to their PrismUserProfileActivity
         */
        private void intentToUserProfileActivity() {
            Intent prismUserProfileIntent = new Intent(context, PrismUserProfileActivity.class);
            prismUserProfileIntent.putExtra("PrismUser", prismUser);
            context.startActivity(prismUserProfileIntent);
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        /**
         *
         */
        private void setupUserFollowButton() {
            if (!Helper.isPrismUserCurrentUser(prismUser)) {
                userFollowButton.setVisibility(View.VISIBLE);

                toggleFollowButtons(CurrentUser.isFollowingPrismUser(prismUser));

                userFollowButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean performFollow = !CurrentUser.isFollowingPrismUser(prismUser);
                        handleFollowButtonClick(performFollow);
                    }
                });
            }
        }

        /**
         *
         */
        private void toggleFollowButtons(boolean showFollowing) {
            int buttonWidth = (int) (scale * (showFollowing ? 80 : 60));
            String followButtonString = showFollowing ? "Following" : "Follow";
            int followButtonInt = showFollowing ? R.drawable.button_selector_selected : R.drawable.button_selector;
            Drawable followingButtonDrawable = context.getResources().getDrawable(followButtonInt);
            Drawable followingToolbarButtonDrawable = context.getResources().getDrawable(followButtonInt);

            userFollowButton.setText(followButtonString);
            userFollowButton.setBackground(followingButtonDrawable);

            userFollowButton.getLayoutParams().width = buttonWidth;
            userFollowButton.setText(followButtonString);
            userFollowButton.setBackground(followingToolbarButtonDrawable);
            userFollowButton.requestLayout();
        }

        /**
         *
         * @param performFollow
         */
        private void handleFollowButtonClick(boolean performFollow) {
            if (performFollow) {
                toggleFollowButtons(true);
                DatabaseAction.followUser(prismUser);
            } else {
                toggleFollowButtons(false);
                DatabaseAction.unfollowUser(prismUser);
            }
        }

        /**
         * Setup the userProfilePicImageView so it is populated with a Default or custom picture
         */
        private void setupUserProfilePicImageView() {
            if (prismUser.getProfilePicture() != null) {
                Glide.with(context)
                        .asBitmap()
                        .thumbnail(0.05f)
                        .load(prismUser.getProfilePicture().lowResUri)
                        .into(new BitmapImageViewTarget(userProfilePicture) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                if (!prismUser.getProfilePicture().isDefault) {
                                    int whiteOutlinePadding = (int) (1 * scale);
                                    userProfilePicture.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                                    userProfilePicture.setBackground(context.getResources().getDrawable(R.drawable.circle_profile_frame));
                                } else {
                                    userProfilePicture.setPadding(0, 0, 0, 0);
                                    userProfilePicture.setBackground(null);
                                }

                                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                                drawable.setCircular(true);
                                userProfilePicture.setImageDrawable(drawable);
                            }
                        });
            }
        }

        /**
         * Setup the TextViews for LikeRepost item
         */
        private void setupUsernameAndFullNameTextView() {
            usernameTextView.setText(prismUser.getUsername());
            userFullNameText.setText(prismUser.getFullName());
        }

        /**
         * Setup all UI elements
         */
        private void setupUIElements() {
            // Setup Typefaces for all text based UI elements
            usernameTextView.setTypeface(sourceSansProBold);
            userFullNameText.setTypeface(sourceSansProLight);
            userFollowButton.setTypeface(sourceSansProLight);

            setupUserProfilePicImageView();
            setupUsernameAndFullNameTextView();
            setupUserFollowButton();
            setupUserRelativeLayout();
        }
    }
}
