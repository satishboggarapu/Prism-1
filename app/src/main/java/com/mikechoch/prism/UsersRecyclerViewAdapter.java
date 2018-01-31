package com.mikechoch.prism;

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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/21/18.
 */

public class UsersRecyclerViewAdapter extends RecyclerView.Adapter<UsersRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<PrismUser> prismUserArrayList;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    public UsersRecyclerViewAdapter(Context context, ArrayList<PrismUser> prismUserArrayList) {
        this.context = context;
        this.prismUserArrayList = prismUserArrayList;

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

        private PrismUser prismUser;
        private ImageView userProfilePicture;
        private TextView usernameTextView;

        private FirebaseAuth auth;
        private DatabaseReference userReference;

        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

            userProfilePicture = itemView.findViewById(R.id.user_profile_picture_image_view);
            usernameTextView = itemView.findViewById(R.id.username_text_view);
        }

        public void setData(PrismUser prismUser) {
            this.prismUser = prismUser;

            Glide.with(context)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(this.prismUser.getProfilePicture())
                    .apply(new RequestOptions().centerCrop())
                    .into(new BitmapImageViewTarget(userProfilePicture) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                            drawable.setCircular(true);
                            userProfilePicture.setImageDrawable(drawable);
                        }
                    });

            usernameTextView.setText(prismUser.getUsername());
            usernameTextView.setTypeface(sourceSansProLight);

        }
    }
}
