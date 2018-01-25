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

    private ItemListener mListener;

    private int screenWidth;
    private int screenHeight;

    public RecyclerViewAdapter(Context context, ArrayList<String> wallpaperKeys, HashMap<String, Wallpaper> wallpaperHashMap, ItemListener listener, int[] screenDimens) {
        this.context = context;
        this.wallpaperKeys = wallpaperKeys;
        this.wallpaperHashMap = wallpaperHashMap;
        this.mListener = listener;
        this.screenWidth = screenDimens[0];
        this.screenHeight = screenDimens[1];

        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    public void setListener(ItemListener listener) {
        mListener = listener;
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            likeBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            shareBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            moreBounceAnimation = AnimationUtils.loadAnimation(context, R.anim.button_bounce_animation);
            // Use bounce interpolator with amplitude 0.2 and frequency 20
            interpolator = new AnimationBounceInterpolator(0.2, 20);
            likeBounceAnimation.setInterpolator(interpolator);
            shareBounceAnimation.setInterpolator(interpolator);
            moreBounceAnimation.setInterpolator(interpolator);

            progressBar = itemView.findViewById(R.id.image_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            wallpaperUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);
            wallpaperUserTextView.setTypeface(sourceSansProBold);

            wallpaperImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
            wallpaperImageView.getLayoutParams().width = (int) (screenWidth * 0.9);
            wallpaperImageView.setMaxHeight((int) (screenHeight * 0.6));

            likeButton = itemView.findViewById(R.id.image_like_button);
            // TODO: get wallpaper current like status and show correct heart
            final boolean[] liked = {false};
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
                        likeButton.startAnimation(likeBounceAnimation);

                        // TODO: change this to modify the specific image to be liekd or unliked
                        liked[0] = !liked[0];
                }
            });

            shareButton = itemView.findViewById(R.id.image_share_button);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    shareButton.startAnimation(shareBounceAnimation);

                    // TODO: share intent
                }
            });

            moreButton = itemView.findViewById(R.id.image_more_button);
            moreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moreButton.startAnimation(moreBounceAnimation);

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
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable() {
                @Override
                public void run() {

//                    Picasso.with(context).load(wallpaper.getImage()).into(wallpaperImageView, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            progressBar.setVisibility(View.INVISIBLE);
//                        }
//
//                        @Override
//                        public void onError() {
//
//                        }
//                    });
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                mListener.onItemClick(wallpaper);
            }
        }
    }

    public interface ItemListener {
        void onItemClick(Wallpaper item);
    }
}
