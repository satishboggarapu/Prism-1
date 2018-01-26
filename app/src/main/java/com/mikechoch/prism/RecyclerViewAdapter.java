package com.mikechoch.prism;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by mikechoch on 1/21/18.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private Wallpaper wallpaper;
    private ArrayList<String> wallpaperKeys;
    private HashMap<String, Wallpaper> wallpaperHashMap;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private int screenWidth;
    private int screenHeight;

    public RecyclerViewAdapter(Context context, ArrayList<String> wallpaperKeys, HashMap<String, Wallpaper> wallpaperHashMap, int[] screenDimens) {
        this.context = context;
        this.wallpaperKeys = wallpaperKeys;
        this.wallpaperHashMap = wallpaperHashMap;
        this.screenWidth = screenDimens[0];
        this.screenHeight = screenDimens[1];

        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(wallpaperHashMap.get(wallpaperKeys.get(position)));
    }

    @Override
    public int getItemCount() {
        return wallpaperHashMap.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Wallpaper wallpaper;
        private TextView wallpaperUserTextView;
        private ImageView wallpaperImageView;
        private ImageView likeButton;
        private ImageView shareButton;
        private ImageView moreButton;
        private ProgressBar progressBar;

        private Animation likeBounceAnimation;
        private Animation shareBounceAnimation;
        private Animation moreBounceAnimation;
        private AnimationBounceInterpolator interpolator;

        private DatabaseReference userReference;
        private FirebaseAuth auth;

        public ViewHolder(View itemView) {
            super(itemView);

            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());



            progressBar = itemView.findViewById(R.id.image_progress_bar);

            wallpaperUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);


            wallpaperImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
            wallpaperImageView.getLayoutParams().width = (int) (screenWidth * 0.9);
            wallpaperImageView.setMaxHeight((int) (screenHeight * 0.6));

            likeButton = itemView.findViewById(R.id.image_like_button);

            shareButton = itemView.findViewById(R.id.image_share_button);

            moreButton = itemView.findViewById(R.id.image_more_button);
        }

        public void setData(Wallpaper wallpaperObject) {
            this.wallpaper = wallpaperObject;
            wallpaperUserTextView.setText(wallpaper.getUsername());
            Glide.with(context)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(wallpaper.getImage())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;           // TODO: @Mike we should hide progressBar here as well and display a toast or something
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.INVISIBLE); // TODO: @Mike shouldn't this be View.GONE?
                            return false;
                        }
                    })
                    .into(wallpaperImageView);

            likeBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            shareBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            moreBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            // Use bounce interpolator with amplitude 0.2 and frequency 20
            interpolator = new AnimationBounceInterpolator(0.2, 20);
            likeBounceAnimation.setInterpolator(interpolator);
            shareBounceAnimation.setInterpolator(interpolator);
            moreBounceAnimation.setInterpolator(interpolator);

            progressBar.setVisibility(View.VISIBLE);

            wallpaperUserTextView.setTypeface(sourceSansProBold);

            // TODO: get wallpaper current like status and show correct heart
            String postId = this.wallpaper.getPostid();
            boolean postLiked = CurrentUser.userLikedPosts.containsKey(postId);

            Drawable heartDrawable = context.getResources().getDrawable(
                    postLiked ? R.drawable.ic_heart_black_36dp : R.drawable.ic_heart_outline_black_36dp);
            int color = context.getResources().getColor(
                    postLiked ? R.color.colorAccent : android.R.color.white);
            heartDrawable.setTint(color);
            likeButton.setImageDrawable(heartDrawable);
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String postId = wallpaper.getPostid();
                    boolean postLiked = CurrentUser.userLikedPosts.containsKey(postId);
                    Drawable heartDrawable = context.getResources().getDrawable(
                            postLiked ? R.drawable.ic_heart_outline_black_36dp : R.drawable.ic_heart_black_36dp);
                    int color = context.getResources().getColor(
                            postLiked ? android.R.color.white : R.color.colorAccent);
                    heartDrawable.setTint(color);
                    likeButton.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeBounceAnimation);

                    // TODO: change this to modify the specific image to be liked or unliked
                    handleLikeButtonClick(wallpaper);

                }
            });

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareButton.startAnimation(shareBounceAnimation);

                    // TODO: share intent
                }
            });

            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreButton.startAnimation(moreBounceAnimation);

                    // TODO: show more menu
                }
            });
        }

        /**
         * Check userLikedPosts HashMap if it contains the postId or not. If it contains
         * the postId, then user has already liked the post and perform UNLIKE operation
         * If it doesn't exist, user has not liked it yet, and perform LIKE operation
         * operation LIKE (performLike = true): increment like count in DB (+1) and add the post
         * id to the userLikedPosts HashMap with current timestamp as value
         * operation UNLIKE (performLike = false): decrement like count in DB (-1) and remove
         * the item from the userLikedPosts HashMap
         */
        private void handleLikeButtonClick(Wallpaper wallpaper) {
            String postId = wallpaper.getPostid();
            long timestamp = Calendar.getInstance().getTimeInMillis();
            boolean performLike = !CurrentUser.userLikedPosts.containsKey(postId);

            DatabaseReference postReference = Default.ALL_POSTS_REFERENCE.child(postId);

            postReference.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    Wallpaper w = mutableData.getValue(Wallpaper.class);
                    if (w == null) {
                        mutableData.setValue(0);
                    } else {
                        int likes = w.getLikes();
                        if (performLike) {
                            // increment likes count
                            mutableData.child(Key.POST_LIKES).setValue(likes + 1);
                            // add postId to user's liked section
                            userReference.child(Key.DB_REF_USER_LIKES).child(postId).setValue(timestamp);
                            // add postId and timestamp to userLikedPosts hashMap
                            CurrentUser.userLikedPosts.put(postId, timestamp);
                        } else {
                            int dislikeCount = likes == 0 ? 0 : likes - 1;
                            mutableData.child(Key.POST_LIKES).setValue(dislikeCount);
                            userReference.child(Key.DB_REF_USER_LIKES).child(postId).removeValue();
                            CurrentUser.userLikedPosts.remove(postId);
                        }
                    }
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });
        }

    }

}
