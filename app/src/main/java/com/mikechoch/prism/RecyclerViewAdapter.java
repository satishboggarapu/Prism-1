package com.mikechoch.prism;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.MotionEvent;
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
        private ImageView likeHeartAnimationImageView;
        private TextView likesCountTextView;
        private ImageView likeButton;
        private TextView repostsCountTextView;
        private ImageView repostButton;
        private ImageView moreButton;
        private ProgressBar progressBar;

        private Animation likeHeartBounceAnimation;
        private Animation likeButtonBounceAnimation;
        private Animation shareButtonBounceAnimation;
        private Animation moreButtonBounceAnimation;
        private AnimationBounceInterpolator interpolator;

        private DatabaseReference userReference;
        private FirebaseAuth auth;

        public ViewHolder(View itemView) {
            super(itemView);

            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

            progressBar = itemView.findViewById(R.id.image_progress_bar);

            likeHeartBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.like_heart_animation);
            likeButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            shareButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            moreButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);

            wallpaperUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);
            wallpaperImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
            likeHeartAnimationImageView = itemView.findViewById(R.id.recycler_view_like_heart);

            likeButton = itemView.findViewById(R.id.image_like_button);
            repostButton = itemView.findViewById(R.id.image_repost_button);
            moreButton = itemView.findViewById(R.id.image_more_button);

            likesCountTextView = itemView.findViewById(R.id.likes_count_text_view);
            repostsCountTextView = itemView.findViewById(R.id.shares_count_text_view);
        }

        public void setData(Wallpaper wallpaperObject) {
            this.wallpaper = wallpaperObject;

            /*
             * Post ID
             */
            String postId = this.wallpaper.getPostid();

            /*
             * Username
             */
            wallpaperUserTextView.setText(wallpaper.getUsername());
            wallpaperUserTextView.setTypeface(sourceSansProBold);

            /*
             * Image
             */
            progressBar.setVisibility(View.VISIBLE);
            wallpaperImageView.getLayoutParams().width = (int) (screenWidth * 0.9);
            wallpaperImageView.setMaxHeight((int) (screenHeight * 0.6));
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    System.out.println("Single");
                    // TODO: Intent to new Activity for image
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    System.out.println("Long");
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    // TODO: This is the double tap for liking images
                    // TODO: Make sure all cloud shit is handled properly
                    Drawable heartDrawable = createHeartDrawable(true);
                    likeButton.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeButtonBounceAnimation);
                    likeHeartAnimationImageView.startAnimation(likeHeartBounceAnimation);

                    handleLikeButtonClick(wallpaper);
                    return super.onDoubleTap(e);
                }
            });
            wallpaperImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    gestureDetector.onTouchEvent(motionEvent);
                    return true;
                }
            });
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

            /*
             * Animation
             */
            // Use bounce interpolator with amplitude 0.2 and frequency 20
            interpolator = new AnimationBounceInterpolator(0.2, 20);
            likeButtonBounceAnimation.setInterpolator(interpolator);
            shareButtonBounceAnimation.setInterpolator(interpolator);
            moreButtonBounceAnimation.setInterpolator(interpolator);

            likeHeartBounceAnimation.setInterpolator(interpolator);
            likeHeartBounceAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    likeHeartAnimationImageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    likeHeartAnimationImageView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            /*
             * Like
             */
            int likeCount = this.wallpaper.getLikes();
            likesCountTextView.setText(likeCount + " like(s)");
            likesCountTextView.setTypeface(sourceSansProLight);

            boolean postLiked = CurrentUser.userLikedPosts.containsKey(postId);
            Drawable heartDrawable = createHeartDrawable(postLiked);
            likeButton.setImageDrawable(heartDrawable);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String postId = wallpaper.getPostid();
                    boolean postLiked = CurrentUser.userLikedPosts.containsKey(postId);
                    Drawable heartDrawable = createHeartDrawable(postLiked);
                    likeButton.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeButtonBounceAnimation);

                    handleLikeButtonClick(wallpaper);

                }
            });

            /*
             * Repost
             */
//            int repostCount = this.wallpaper.getReposts();
            int repostCount = 4;
            repostsCountTextView.setText(repostCount + " share(s)");
            repostsCountTextView.setTypeface(sourceSansProLight);

//            boolean postReposted = CurrentUser.userRepostedPost.containsKey(postId);
            final boolean[] reposted = {false};
            repostButton = itemView.findViewById(R.id.image_repost_button);
            int repostColor = context.getResources().getColor(
                    reposted[0] ? R.color.colorAccent : android.R.color.white);
            repostButton.setImageTintList(ColorStateList.valueOf(repostColor));
            repostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder repostConfirmationAlertDialogBuilder = new AlertDialog.Builder(context);
                    repostConfirmationAlertDialogBuilder.setTitle("This post will show on your profile, are you sure you want to repost?");
                    repostConfirmationAlertDialogBuilder.setPositiveButton("REPOST", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            reposted[0] = !reposted[0];
                            // TODO: change this to modify the specific image to be reposted or un-reposted

                            int repostColor = context.getResources().getColor(
                                    reposted[0] ? R.color.colorAccent : android.R.color.white);
                            repostButton.setImageTintList(ColorStateList.valueOf(repostColor));
                            repostButton.startAnimation(shareButtonBounceAnimation);
                            dialogInterface.dismiss();
                        }
                    }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog repostConfirmationAlertDialog = repostConfirmationAlertDialogBuilder.create();
                    repostConfirmationAlertDialog.show();
                }
            });

            /*
             * More
             */
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreButton.startAnimation(moreButtonBounceAnimation);
                    // TODO: show more menu
                }
            });
        }

        /**
         *
         */
        private Drawable createHeartDrawable(boolean isLiked) {
            int heart = isLiked ? R.drawable.ic_heart_black_36dp : R.drawable.ic_heart_outline_black_36dp;
            int color = isLiked ? R.color.colorAccent : android.R.color.white;
            Drawable heartDrawable = context.getResources().getDrawable(heart);
            int heartColor = context.getResources().getColor(color);
            heartDrawable.setTint(heartColor);
            return heartDrawable;
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

                            // add the user to LIKED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_LIKED_USERS)
                                    .child(CurrentUser.user.getUid())
                                    .setValue(CurrentUser.user.getDisplayName());
                        } else {
                            int dislikeCount = likes == 0 ? 0 : likes - 1;

                            // decrement likes count
                            mutableData.child(Key.POST_LIKES).setValue(dislikeCount);

                            // remove postId from user's liked section
                            userReference.child(Key.DB_REF_USER_LIKES).child(postId).removeValue();

                            // remove the postId and timestamp from userLikedPosts hashMap
                            CurrentUser.userLikedPosts.remove(postId);

                            // remove the user from LIKED_USERS list for this post
                            postReference.child(Key.DB_REF_POST_LIKED_USERS)
                                    .child(CurrentUser.user.getUid())
                                    .removeValue();
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
