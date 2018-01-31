package com.mikechoch.prism.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.LoginActivity;

import java.lang.reflect.Type;

/**
 * Created by mikechoch on 1/22/18.
 */

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;

    private ImageView userProfilePicImageView;
    private Button logoutButton;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;


    public static final ProfileFragment newInstance(int title, String message) {
        ProfileFragment profileFragment = new ProfileFragment();
        Bundle bundle = new Bundle(2);
        bundle.putInt("Title", title);
        bundle.putString("Extra_Message", message);
        profileFragment.setArguments(bundle);
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int title = getArguments().getInt("Title");
        String message = getArguments().getString("Extra_Message");

        auth = FirebaseAuth.getInstance();

        sourceSansProLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Black.ttf");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment_layout, container, false);

        userProfilePicImageView = view.findViewById(R.id.profile_frag_profile_picture_image_view);

        Glide.with(this)
                .asBitmap()
                .thumbnail(0.05f)
                .load(R.drawable.default_prof_0)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(userProfilePicImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                        drawable.setCircular(true);
                        userProfilePicImageView.setImageDrawable(drawable);
                    }
                });



        logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        logoutButton.setTypeface(sourceSansProLight);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }
}
