package com.mikechoch.prism.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.mikechoch.prism.R;
import com.mikechoch.prism.attribute.PrismPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mikechoch on 2/7/18.
 */

public class StaggeredGridRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredGridRecyclerViewAdapter.ViewHolder> {

    /*
     * Global variables
     */
    private Context context;
    private List<PrismPost> prismPostsArrayList;


    public StaggeredGridRecyclerViewAdapter(Context context, ArrayList<PrismPost> prismPostsArrayList) {
        this.context = context;
        this.prismPostsArrayList = prismPostsArrayList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.staggered_grid_user_post_recycler_view_item, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(prismPostsArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return this.prismPostsArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView userPostImageView;

        private PrismPost prismPost;


        public ViewHolder(View itemView) {
            super(itemView);
            userPostImageView = itemView.findViewById(R.id.user_post_image_view);
        }

        /**
         * Set data for the ViewHolder UI elements
         */
        public void setData(PrismPost prismPost) {
            this.prismPost = prismPost;
            populateUIElements();
        }

        /**
         * Setup userPostImageView
         * Populate userPostImageView using Glide with the specific post image
         */
        private void setupPostImageView() {
            Glide.with(context)
                    .asBitmap()
                    .load(prismPost.getImage())
                    .apply(new RequestOptions().fitCenter())
                    .into(userPostImageView);
        }

        /**
         * Populate all UI elements with data
         */
        private void populateUIElements() {
            setupPostImageView();
        }
    }
}