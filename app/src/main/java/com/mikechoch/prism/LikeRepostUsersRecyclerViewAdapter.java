package com.mikechoch.prism;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mikechoch on 1/21/18.
 */

public class LikeRepostUsersRecyclerViewAdapter extends RecyclerView.Adapter<LikeRepostUsersRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PrismUser> prismUserArrayList;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private float scale;

    public LikeRepostUsersRecyclerViewAdapter(Context context, ArrayList<PrismUser> prismUserArrayList) {
        this.context = context;
        this.prismUserArrayList = prismUserArrayList;

        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");

        scale = context.getResources().getDisplayMetrics().density;
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

        private PrismUser prismUser;
        private ImageView userProfilePicture;
        private TextView usernameTextView;
        private TextView userFullNameText;

        private FirebaseAuth auth;
        private DatabaseReference userReference;

        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

            userProfilePicture = itemView.findViewById(R.id.user_profile_picture_image_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
            userFullNameText = itemView.findViewById(R.id.full_name_text_view);
        }

        public void setData(PrismUser prismUser) {
            this.prismUser = prismUser;
            Random random = new Random();
            int defaultProfPic = random.nextInt(10);
            Uri uri = Uri.parse(String.valueOf(DefaultProfilePicture.values()[defaultProfPic].getProfilePicture()));
            Glide.with(context)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(prismUser.getProfilePicture() != null ? prismUser.getProfilePicture() : uri)
                    .into(new BitmapImageViewTarget(userProfilePicture) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            if (prismUser.getProfilePicture() != null) {
                                int whiteOutlinePadding = (int) (1 * scale);
                                userProfilePicture.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
                                userProfilePicture.setBackground(context.getResources().getDrawable(R.drawable.circle_profile_frame));
                            }

                            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            drawable.setCircular(true);
                            userProfilePicture.setImageDrawable(drawable);
                        }
                    });

            usernameTextView.setText(prismUser.getUsername());
            usernameTextView.setTypeface(sourceSansProBold);

            userFullNameText.setText(prismUser.getFullName());
            userFullNameText.setTypeface(sourceSansProLight);

        }
    }
}
