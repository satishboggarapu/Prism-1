package com.mikechoch.prism.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.mikechoch.prism.R;
import com.mikechoch.prism.SettingsOption;
import com.mikechoch.prism.activity.LoginActivity;
import com.mikechoch.prism.attribute.PrismPost;
import com.mikechoch.prism.constants.Default;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mikechoch on 2/7/18.
 */

public class SettingsOptionRecyclerViewAdapter extends RecyclerView.Adapter<SettingsOptionRecyclerViewAdapter.ViewHolder> {

    /*
     * Global variables
     */
    private Context context;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private FirebaseAuth auth;


    public SettingsOptionRecyclerViewAdapter(Context context) {
        this.context = context;

        this.sourceSansProLight = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Light.ttf");
        this.sourceSansProBold = Typeface.createFromAsset(context.getAssets(), "fonts/SourceSansPro-Black.ttf");

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.settings_recycler_view_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setData(SettingsOption.values()[position]);
    }

    @Override
    public int getItemCount() {
        return SettingsOption.values().length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout settingsOptionRelativeLayout;
        private TextView settingsOptionTextView;
        private ImageView settingsOptionImageView;

        private SettingsOption settingsOption;


        public ViewHolder(View itemView) {
            super(itemView);

            // SettingOptions UI initializations
            settingsOptionRelativeLayout = itemView.findViewById(R.id.settings_recycler_view_item_relative_layout);
            settingsOptionTextView = itemView.findViewById(R.id.settings_recycler_view_item_text_view);
            settingsOptionImageView = itemView.findViewById(R.id.settings_recycler_view_item_icon);
        }

        /**
         * Set data for the ViewHolder UI elements
         */
        public void setData(SettingsOption settingsOption) {
            this.settingsOption = settingsOption;
            populateUIElements();
        }

        /**
         * settingsOptionRelativeLayout
         * Set the onClickListener switch statement for each SettingsOption
         */
        private void setupSettingsOptionRelativeLayout() {
            settingsOptionRelativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int settingOptionId = settingsOption.getOptionId();
                    switch(settingOptionId) {
                        case Default.SETTINGS_OPTION_APP:
                            break;
                        case Default.SETTINGS_OPTION_NOTIFICATION:
                            break;
                        case Default.SETTINGS_OPTION_ACCOUNT:
                            break;
                        case Default.SETTINGS_OPTION_HELP:
                            break;
                        case Default.SETTINGS_OPTION_ABOUT:
                            break;
                        case Default.SETTINGS_OPTION_LOGOUT:
                            auth.signOut();
                            Intent intent = new Intent(context, LoginActivity.class);
                            context.startActivity(intent);
                            ((Activity) context).finish();
                            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        /**
         * settingsOptionTextView
         * Get the SettingsOption enum title and populate the TextView
         */
        private void setupSettingsOptionTextView() {
            settingsOptionTextView.setText(settingsOption.getOptionTitle());
        }

        /**
         * settingsOptionImageView
         * Get the SettingsOption enum icon and populate the ImageView
         */
        private void setupSettingsOptionImageView() {
            Drawable settingsIcon = context.getResources().getDrawable(settingsOption.getOptionIcon());
            settingsIcon.setTint(Color.WHITE);
            settingsOptionImageView.setImageDrawable(settingsIcon);
        }

        /**
         * Populate all UI elements with data
         */
        private void populateUIElements() {
            // Setup Typefaces for all text based UI elements
            settingsOptionTextView.setTypeface(sourceSansProLight);

            setupSettingsOptionRelativeLayout();
            setupSettingsOptionTextView();
            setupSettingsOptionImageView();
        }
    }
}