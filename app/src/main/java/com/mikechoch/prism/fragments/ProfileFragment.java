package com.mikechoch.prism.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.mikechoch.prism.CurrentUser;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.DefaultProfilePicture;
import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.LoginActivity;
import com.mikechoch.prism.activity.ProfilePictureUploadActivity;

import java.util.Random;

/**
 * Created by mikechoch on 1/22/18.
 */

public class ProfileFragment extends Fragment {

    private FirebaseAuth auth;
    private DatabaseReference userReference;

    private ImageView userProfilePicImageView;
    private TextView userUsernameTextView;
    private TextView userFullNameTextView;
    private Button logoutButton;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private float scale;
    private String[] setProfilePicStrings = {"Choose from gallery", "Take a selfie"};


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

        scale = this.getResources().getDisplayMetrics().density;

        auth = FirebaseAuth.getInstance();
        userReference = Default.USERS_REFERENCE.child(auth.getCurrentUser().getUid());

        sourceSansProLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/SourceSansPro-Black.ttf");

    }

    @SuppressLint("NewApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment_layout, container, false);

        userProfilePicImageView = view.findViewById(R.id.profile_frag_profile_picture_image_view);
        Random random = new Random();
        int defaultProfPic = random.nextInt(10);
        Uri uri = Uri.parse(String.valueOf(DefaultProfilePicture.values()[defaultProfPic].getProfilePicture()));
        Glide.with(this)
                .asBitmap()
                .thumbnail(0.05f)
                .load(uri)
                .apply(new RequestOptions().fitCenter())
                .into(new BitmapImageViewTarget(userProfilePicImageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getActivity().getResources(), resource);
                        drawable.setCircular(true);
                        userProfilePicImageView.setImageDrawable(drawable);

//                        if (profilePic != null) {
//                            int whiteOutlinePadding = (int) (1 * scale);
//                            userProfilePicImageView.setPadding(whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding, whiteOutlinePadding);
//                            userProfilePicImageView.setBackground(getActivity().getResources().getDrawable(R.drawable.circle_profile_frame));
//                        }
                    }
                });
        userProfilePicImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog setProfilePictureAlertDialog = createSetProfilePictureAlertDialog();
                setProfilePictureAlertDialog.show();
            }
        });

        userUsernameTextView = view.findViewById(R.id.profile_frag_username_text_view);
        userUsernameTextView.setText(CurrentUser.username);
        userUsernameTextView.setTypeface(sourceSansProBold);
        userFullNameTextView = view.findViewById(R.id.profile_frag_full_name_text_view);
        userFullNameTextView.setText(CurrentUser.user_full_name);
        userFullNameTextView.setTypeface(sourceSansProLight);

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

    /**
     *
     */
    private AlertDialog createSetProfilePictureAlertDialog() {
        AlertDialog.Builder profilePictureAlertDialog = new AlertDialog.Builder(getActivity());
        profilePictureAlertDialog.setTitle("Set profile picture");
        profilePictureAlertDialog.setItems(setProfilePicStrings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        Intent imageUploadIntent = new Intent(getActivity(), ProfilePictureUploadActivity.class);
                        getActivity().startActivityForResult(imageUploadIntent, Default.PROFILE_PIC_UPLOAD_INTENT_REQUEST_CODE);
                        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        break;
                    case 1:
                        // TODO: Figure out camera feature
                        break;
                    default:
                        break;
                }
            }
        });

        return profilePictureAlertDialog.create();
    }

}
