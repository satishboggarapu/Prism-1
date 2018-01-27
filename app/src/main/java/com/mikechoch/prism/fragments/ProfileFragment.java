package com.mikechoch.prism.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.LoginActivity;

import java.lang.reflect.Type;

/**
 * Created by mikechoch on 1/22/18.
 */

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;

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
