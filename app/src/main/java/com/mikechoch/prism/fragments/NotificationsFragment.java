package com.mikechoch.prism.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikechoch.prism.R;

/**
 * Created by mikechoch on 1/22/18.
 */

public class NotificationsFragment extends Fragment {

    public static final NotificationsFragment newInstance(int title, String message) {
        NotificationsFragment notificationsFragment = new NotificationsFragment();
        Bundle bundle = new Bundle(2);
        bundle.putInt("Title", title);
        bundle.putString("Extra_Message", message);
        notificationsFragment.setArguments(bundle);
        return notificationsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int title = getArguments().getInt("Title");
        String message = getArguments().getString("Extra_Message");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment_layout, container, false);
        return view;
    }
}
