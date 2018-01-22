package com.mikechoch.prism;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by mikechoch on 1/21/18.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private int displayWidth;
    private int displayHeight;
    private List<Wallpaper> mItems;
    private Wallpaper currentWallpaper;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private ItemListener mListener;

    public RecyclerViewAdapter(Context context, List<Wallpaper> items, ItemListener listener, int displayWidth, int displayHeight) {
        this.context = context;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.mItems = items;
        this.mListener = listener;

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
        holder.setData(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Wallpaper wallpaper;
        private TextView wallpaperCaptionTextView;
        private TextView wallpaperUserTextView;
        private ImageView wallpaperImageView;
        private RelativeLayout relativeLayout;
        private ProgressBar progressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            progressBar = itemView.findViewById(R.id.image_progress_bar);
            progressBar.getLayoutParams().height = displayWidth;

            relativeLayout = itemView.findViewById(R.id.recycler_view_frame);
//            relativeLayout.getLayoutParams().width = (int) (displayWidth * 0.7);
//            relativeLayout.getLayoutParams().height = (int) (displayHeight * 0.4);

            wallpaperUserTextView = itemView.findViewById(R.id.recycler_view_user_text_view);
            wallpaperUserTextView.setTypeface(sourceSansProBold);
            wallpaperCaptionTextView = itemView.findViewById(R.id.recycler_view_caption_text_view);
            wallpaperCaptionTextView.setTypeface(sourceSansProLight);
            wallpaperImageView = itemView.findViewById(R.id.recycler_view_image_image_view);
//            wallpaperImageView.getLayoutParams().width = (int) (displayWidth * 0.7);
//            wallpaperImageView.getLayoutParams().height = (int) (displayHeight * 0.4);

        }

        public void setData(final Wallpaper wallpaper) {
            this.wallpaper = wallpaper;
            wallpaperUserTextView.setText("username");
            wallpaperCaptionTextView.setText(wallpaper.getCaption());
            Handler uiHandler = new Handler(Looper.getMainLooper());
            uiHandler.post(new Runnable(){
                @Override
                public void run() {
                    Picasso.with(context).load(wallpaper.getImageUri()).into(wallpaperImageView);
//                            , new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            progressBar.setVisibility(View.GONE);
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
