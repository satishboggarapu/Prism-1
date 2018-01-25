package com.mikechoch.prism;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by mikechoch on 1/21/18.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
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
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_item_layout, parent, false));
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
        private TextView sharesCountTextView;
        private ImageView shareButton;
        private ImageView moreButton;
        private ProgressBar progressBar;

        private Animation likeHeartBounceAnimation;
        private Animation likeButtonBounceAnimation;
        private Animation shareButtonBounceAnimation;
        private Animation moreButtonBounceAnimation;
        private AnimationBounceInterpolator interpolator;

        public ViewHolder(View itemView) {
            super(itemView);

            likeHeartBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.like_heart_animation);
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

            likeButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            shareButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            moreButtonBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            // Use bounce interpolator with amplitude 0.2 and frequency 20
            interpolator = new AnimationBounceInterpolator(0.2, 20);
            likeButtonBounceAnimation.setInterpolator(interpolator);
            shareButtonBounceAnimation.setInterpolator(interpolator);
            moreButtonBounceAnimation.setInterpolator(interpolator);

            final boolean[] liked = {false};
            final GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    System.out.println("Single");
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    System.out.println("Long");
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {

                    Drawable heartDrawable = context.getResources().getDrawable(R.drawable.ic_heart_black_36dp);
                    int color = context.getResources().getColor(R.color.colorAccent);
                    heartDrawable.setTint(color);
                    likeButton.setImageDrawable(heartDrawable);
                    likeButton.startAnimation(likeButtonBounceAnimation);

                    likeHeartAnimationImageView.startAnimation(likeHeartBounceAnimation);

                    // TODO: change this to modify the specific image to be liked
                    if (!liked[0]) {
                        liked[0] = !liked[0];
                    }
                    return super.onDoubleTap(e);
                }
            });

            progressBar = itemView.findViewById(R.id.image_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            wallpaperUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);
            wallpaperUserTextView.setTypeface(sourceSansProBold);

            wallpaperImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
            wallpaperImageView.getLayoutParams().width = (int) (screenWidth * 0.9);
            wallpaperImageView.setMaxHeight((int) (screenHeight * 0.6));
            wallpaperImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    gestureDetector.onTouchEvent(motionEvent);
                    return true;
                }
            });

            likeHeartAnimationImageView = itemView.findViewById(R.id.recycler_view_like_heart);

            likesCountTextView = itemView.findViewById(R.id.likes_count_text_view);
            likesCountTextView.setTypeface(sourceSansProLight);
            int count = 20;
            likesCountTextView.setText(count + " like(s)");

            likeButton = itemView.findViewById(R.id.image_like_button);
            // TODO: get wallpaper current like status and show correct heart
            Drawable heartDrawable = context.getResources().getDrawable(
                    liked[0] ? R.drawable.ic_heart_black_36dp : R.drawable.ic_heart_outline_black_36dp);
            int color = context.getResources().getColor(
                    liked[0] ? R.color.colorAccent : android.R.color.white);
            heartDrawable.setTint(color);
            likeButton.setImageDrawable(heartDrawable);
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Drawable heartDrawable = context.getResources().getDrawable(
                                liked[0] ? R.drawable.ic_heart_outline_black_36dp : R.drawable.ic_heart_black_36dp);
                        int color = context.getResources().getColor(
                                liked[0] ? android.R.color.white : R.color.colorAccent);
                        heartDrawable.setTint(color);
                        likeButton.setImageDrawable(heartDrawable);
                        likeButton.startAnimation(likeButtonBounceAnimation);

                        // TODO: change this to modify the specific image to be liked or un-liked
                        liked[0] = !liked[0];
                }
            });

            sharesCountTextView = itemView.findViewById(R.id.shares_count_text_view);
            sharesCountTextView.setTypeface(sourceSansProLight);
            count = 4;
            sharesCountTextView.setText(count + " share(s)");

            shareButton = itemView.findViewById(R.id.image_share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareButton.startAnimation(shareButtonBounceAnimation);

                    // TODO: share intent
                }
            });

            moreButton = itemView.findViewById(R.id.image_more_button);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreButton.startAnimation(moreButtonBounceAnimation);

                    // TODO: show more menu
                }
            });
        }

        public void setData(final Wallpaper wallpaper) {
            this.wallpaper = wallpaper;
            wallpaperUserTextView.setText("username");
            Glide.with(context)
                    .asBitmap()
                    .thumbnail(0.05f)
                    .load(wallpaper.getImage())
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    })
                    .into(wallpaperImageView);
        }

    }

    class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("onDoubleTap :", "" + e.getAction());
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("onSingleTap :", "" + e.getAction());
            return true;
        }
    }
}
