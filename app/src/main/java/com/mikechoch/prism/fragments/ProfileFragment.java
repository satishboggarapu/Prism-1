package com.mikechoch.prism.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.mikechoch.prism.activity.PrismUserProfileActivity;
import com.mikechoch.prism.adapter.SettingsOptionRecyclerViewAdapter;
import com.mikechoch.prism.attribute.CurrentUser;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.R;

/**
 * Created by mikechoch on 1/22/18.
 */

public class ProfileFragment extends Fragment {

    /*
     * Globals
     */
    private FirebaseAuth auth;
    private DatabaseReference userReference;

    private float scale;
    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private CardView viewProfileCardView;
    private RecyclerView settingsRecyclerView;
    private TextView userFullNameTextView;
    private TextView viewProfileTextView;


    public static final ProfileFragment newInstance() {
        ProfileFragment profileFragment = new ProfileFragment();
        return profileFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

        scale = this.getResources().getDisplayMetrics().density;
        sourceSansProLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment_layout, container, false);

        // Initialize all UI elements
        viewProfileCardView = view.findViewById(R.id.profile_fragment_view_profile_card_view);
        settingsRecyclerView = view.findViewById(R.id.profile_fragment_settings_recycler_view);
        userFullNameTextView = view.findViewById(R.id.profile_fragment_user_full_name_text_view);
        viewProfileTextView = view.findViewById(R.id.profile_fragment_view_profile_text_view);

        viewProfileCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent prismUserProfileIntent = new Intent(getActivity(), PrismUserProfileActivity.class);
                prismUserProfileIntent.putExtra("PrismUser", CurrentUser.prismUser);
                getActivity().startActivity(prismUserProfileIntent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        settingsRecyclerView.setLayoutManager(linearLayoutManager);
        SettingsOptionRecyclerViewAdapter settingsRecyclerViewAdapter = new SettingsOptionRecyclerViewAdapter(getActivity());
        settingsRecyclerView.setAdapter(settingsRecyclerViewAdapter);

        setupUIElements();

        return view;
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        // Setup Typefaces for all text based UI elements
        userFullNameTextView.setTypeface(sourceSansProLight);
        viewProfileTextView.setTypeface(sourceSansProLight);


    }

}
