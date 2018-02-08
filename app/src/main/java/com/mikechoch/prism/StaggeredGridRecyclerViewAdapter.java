package com.mikechoch.prism;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mikechoch on 2/7/18.
 */

public class StaggeredGridRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredGridRecyclerViewAdapter.ViewHolder> {

    private int[] heights = {150, 200, 250, 300, 350, 400};
    private List<Drawable> prismPostsArrayList;
    private Context context;

    public StaggeredGridRecyclerViewAdapter(Context context, ArrayList<Drawable> prismPostsArrayList) {
        this.prismPostsArrayList = prismPostsArrayList;
        this.context = context;
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

        public ViewHolder(View itemView) {
            super(itemView);

            userPostImageView = itemView.findViewById(R.id.user_post_image_view);

        }

        public void setData(Drawable drawable) {
            Glide.with(context)
                    .asBitmap()
                    .load(drawable)
                    .into(userPostImageView);

            int rand = new Random().nextInt(4);
            System.out.println(rand);
            userPostImageView.getLayoutParams().height = (int) (heights[rand] * context.getResources().getDisplayMetrics().density);

        }
    }
}