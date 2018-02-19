package com.mikechoch.prism.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikechoch.prism.R;

/**
 * Created by mikechoch on 1/22/18.
 */

public class NotificationsFragment extends Fragment {

    private RelativeLayout noNotificationsRelativeLayout;
    private TextView noNotificationsTextView;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    public static final NotificationsFragment newInstance() {
        NotificationsFragment notificationsFragment = new NotificationsFragment();
        return notificationsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize normal and bold Prism font
        sourceSansProLight = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceSansPro-Black.ttf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment_layout, container, false);

        noNotificationsRelativeLayout = view.findViewById(R.id.no_notifications_relative_layout);
        noNotificationsTextView = view.findViewById(R.id.no_notifications_text_view);
        noNotificationsTextView.setTypeface(sourceSansProLight);

        return view;
    }
}
