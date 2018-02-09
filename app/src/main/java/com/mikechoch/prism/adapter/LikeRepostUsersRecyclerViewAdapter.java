package com.mikechoch.prism.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.mikechoch.prism.PrismUser;
import com.mikechoch.prism.R;
import com.mikechoch.prism.constants.Default;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/21/18.
 */

public class LikeRepostUsersRecyclerViewAdapter extends RecyclerView.Adapter<LikeRepostUsersRecyclerViewAdapter.ViewHolder> {

    /*
     * Global variables
     */
    private Context context;
    private ArrayList<PrismUser> prismUserArrayList;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;


    public LikeRepostUsersRecyclerViewAdapter(Context context, ArrayList<PrismUser> prismUserArrayList) {
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
                R.layout.like_repost_users_recycler_view_item_layout, parent, false));
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
        private ImageView userProfilePicture;
        private TextView usernameTextView;
        private TextView userFullNameText;

        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

            // Initialize all UI elements
            userProfilePicture = itemView.findViewById(R.id.user_profile_picture_image_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            userFullNameText = itemView.findViewById(R.id.full_name_text_view);
        }

        /**
         * Set data for the ViewHolder UI elements
         */
        public void setData(PrismUser prismUser) {
            this.prismUser = prismUser;
            setupUIElements();
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

            setupUserProfilePicImageView();
            setupUsernameAndFullNameTextView();
        }
    }
}
