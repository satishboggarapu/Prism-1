package com.mikechoch.prism;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
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
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recycler_view_item_layout, parent, false));
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
        private ImageView repostIrisAnimationImageView;
        private TextView repostsCountTextView;
        private ImageView repostButton;
        private ImageView moreButton;
        private ProgressBar progressBar;

        private Animation likeHeartBounceAnimation;
        private Animation repostIrisBounceAnimation;
        private Animation likeButtonBounceAnimation;
        private Animation shareButtonBounceAnimation;
        private Animation moreButtonBounceAnimation;
        private AnimationBounceInterpolator interpolator;

        private DatabaseReference userReference;
        private FirebaseAuth auth;

        public ViewHolder(View itemView) {
            super(itemView);

            // Cloud database initializations
            auth = FirebaseAuth.getInstance();
            userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

            // Animation initializations
            likeHeartBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.like_repost_animation);
            repostIrisBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.like_repost_animation);
            likeButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            shareButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            moreButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);

            // Image initializations
            progressBar = itemView.findViewById(R.id.image_progress_bar);
            wallpaperUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);
            wallpaperImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
            likeHeartAnimationImageView = itemView.findViewById(R.id.recycler_view_like_heart);
            repostIrisAnimationImageView = itemView.findViewById(R.id.recycler_view_repost_iris);

            // Image action button initializations
            likeButton = itemView.findViewById(R.id.image_like_button);
            repostButton = itemView.findViewById(R.id.image_repost_button);
            moreButton = itemView.findViewById(R.id.image_more_button);

            // Image like/repost count initializations
            likesCountTextView = itemView.findViewById(R.id.likes_count_text_view);
            repostsCountTextView = itemView.findViewById(R.id.shares_count_text_view);
        }

        public void setData(Wallpaper wallpaperObject) {
            this.wallpaper = wallpaperObject;

            /*
             * Post ID
             */
            String postId = this.wallpaper.getPostid();
            final int[] likeCount = {this.wallpaper.getLikes()};
//            int repostCount = this.wallpaper.getReposts();
            boolean postLiked = CurrentUser.userLikedPosts.containsKey(postId);
//            boolean postReposted = CurrentUser.userRepostedPost.containsKey(postId);

            /*
             * Username
             */
            wallpaperUserTextView.setText(wallpaper.getUsername());
            wallpaperUserTextView.setTypeface(sourceSansProBold);

            /*
             * Image
             */
            progressBar.setVisibility(View.VISIBLE);

            /*
             * wallpaperImageView will have a width of 90% of the screen
             * wallpaperImageView will have a max height of 60% of the screen
             * This causes any images that are stronger in height to not span the entire screen
             */
            wallpaperImageView.getLayoutParams().width = (int) (screenWidth * 0.9);
            wallpaperImageView.setMaxHeight((int) (screenHeight * 0.6));

            /*
             * GestureDetector used to replace the wallpaperImageView TouchListener
             * This allows detection of Single, Long, and Double tap gestures
             */
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    System.out.println("Image Single Tapped");
                    // TODO: Intent to new Activity for image

                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    System.out.println("Image Long Pressed");
                    // TODO: Think of something to do
                    // TODO: Download image?

                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    System.out.println("Image Double Tapped");

                    String postId = wallpaper.getPostid();
                    boolean postLiked = !CurrentUser.userLikedPosts.containsKey(postId);
                    Drawable heartButtonDrawable = createLikeDrawable(postLiked);
                    likeButton.setImageDrawable(heartButtonDrawable);
                    Drawable heartDrawable = context.getResources().getDrawable(
                            postLiked ? R.drawable.like_heart : R.drawable.unlike_heart);
                    likeHeartAnimationImageView.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeButtonBounceAnimation);
                    likeHeartAnimationImageView.startAnimation(likeHeartBounceAnimation);

                    likeCount[0] += postLiked ? 1 : -1;
                    likesCountTextView.setText(likeCount[0] + " like(s)");

                    handleLikeButtonClick(wallpaper);
                    return super.onDoubleTap(e);
                }
            });

            wallpaperImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    /*
                     * Sets the TouchListener to be handled by the GestureDetector class
                     * This allows detection of Single, Long, and Double tap gestures
                     */
                    gestureDetector.onTouchEvent(motionEvent);
                    return true;
                }
            });

            /*
             * Using the Glide library to populate the wallpaperImageView
             * asBitmap: converts to Bitmap
             * thumbnail: previews a 5% loaded image while the rest of the image is being loaded
             * load: loads the wallpaper URI
             * listener: confirms if the image was uploaded properly or not
             * into: the loaded image will be placed inside the wallpaperImageView
             */
            Glide.with(context)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(wallpaper.getImage())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            // TODO: @Mike we should hide progressBar here as well and display a toast or something
                            progressBar.setVisibility(View.INVISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            // TODO: @Mike we should hide progressBar here as well and display a toast or something
                            progressBar.setVisibility(View.INVISIBLE);
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

            likeHeartAnimationImageView.setVisibility(View.INVISIBLE);
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

            repostIrisAnimationImageView.setVisibility(View.INVISIBLE);
            repostIrisBounceAnimation.setInterpolator(interpolator);
            repostIrisBounceAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    repostIrisAnimationImageView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    repostIrisAnimationImageView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            /*
             * Like
             */
            likesCountTextView.setText(likeCount[0] + " like(s)");
            likesCountTextView.setTypeface(sourceSansProLight);

            Drawable heartButtonDrawable = createLikeDrawable(postLiked);
            likeButton.setImageDrawable(heartButtonDrawable);
            Drawable heartDrawable = context.getResources().getDrawable(
                    postLiked ?  R.drawable.unlike_heart : R.drawable.like_heart);
            likeHeartAnimationImageView.setImageDrawable(heartDrawable);

            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String postId = wallpaper.getPostid();
                    boolean postLiked = !CurrentUser.userLikedPosts.containsKey(postId);
                    Drawable heartButtonDrawable = createLikeDrawable(postLiked);
                    likeButton.setImageDrawable(heartButtonDrawable);
                    Drawable heartDrawable = context.getResources().getDrawable(
                            postLiked ? R.drawable.like_heart : R.drawable.unlike_heart);
                    likeHeartAnimationImageView.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeButtonBounceAnimation);
                    likeHeartAnimationImageView.startAnimation(likeHeartBounceAnimation);

                    likeCount[0] += postLiked ? 1 : -1;
                    likesCountTextView.setText(likeCount[0] + " like(s)");

                    handleLikeButtonClick(wallpaper);
                }
            });

            /*
             * Repost
             */
            int repostCount = 4;
            repostsCountTextView.setText(repostCount + " share(s)");
            repostsCountTextView.setTypeface(sourceSansProLight);

            final boolean[] reposted = {false};
            ColorStateList repostColor = getRepostColor(reposted[0]);
            repostButton.setImageTintList(repostColor);
            repostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!reposted[0]) {
                        AlertDialog repostConfirmationAlertDialog = createRepostConfirmationAlertDialog();
                        repostConfirmationAlertDialog.show();
                    } else {
                        // TODO: Change image to not be reposted

                        ColorStateList repostColor = getRepostColor(false);
                        repostButton.setImageTintList(repostColor);
                        repostButton.startAnimation(shareButtonBounceAnimation);
                    }
                }
            });

            /*
             * More
             */
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreButton.startAnimation(moreButtonBounceAnimation);
                    // TODO: Show more menu
                    // TODO: Decide what goes in more
                }
            });
        }

        /**
         *
         */
        private Drawable createLikeDrawable(boolean isLiked) {
            int heart = isLiked ? R.drawable.ic_heart_black_36dp : R.drawable.ic_heart_outline_black_36dp;
            int color = isLiked ? R.color.colorAccent : android.R.color.white;
            Drawable heartDrawable = context.getResources().getDrawable(heart);
            int heartColor = context.getResources().getColor(color);
            heartDrawable.setTint(heartColor);
            return heartDrawable;
        }

        /**
         *
         */
        private ColorStateList getRepostColor(boolean isReposted) {
            int color = isReposted ? R.color.colorAccent : android.R.color.white;
            int repostColor = context.getResources().getColor(color);
            return ColorStateList.valueOf(repostColor);
        }

        /**
         *
         */
        private AlertDialog createRepostConfirmationAlertDialog() {
            AlertDialog.Builder repostConfirmationAlertDialogBuilder = new AlertDialog.Builder(context);
            repostConfirmationAlertDialogBuilder.setTitle("This post will show on your profile, are you sure you want to repost?");
            repostConfirmationAlertDialogBuilder.setPositiveButton("REPOST", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // TODO: Change image to be reposted

                    ColorStateList repostColor = getRepostColor(true);
                    repostButton.setImageTintList(repostColor);
                    repostButton.startAnimation(shareButtonBounceAnimation);
                    repostIrisAnimationImageView.startAnimation(repostIrisBounceAnimation);

                    dialogInterface.dismiss();
                }
            }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            return repostConfirmationAlertDialogBuilder.create();
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
            // TODO: Double click should force like to true at all times
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
