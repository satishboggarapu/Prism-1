package com.mikechoch.prism.fragments;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikechoch.prism.R;
import com.mikechoch.prism.activity.MainActivity;
import com.mikechoch.prism.activity.SearchActivity;

import org.w3c.dom.Text;

/**
 * Created by mikechoch on 1/22/18.
 */

public class SearchFragment extends Fragment {

    private CardView searchCardView;
    private RelativeLayout searchRelativeLayout;
    private TextView searchByTextView;
    private TextView searchTextView;
    private TextView searchBarHintTextView;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    public static final SearchFragment newInstance() {
        SearchFragment searchFragment = new SearchFragment();
        return searchFragment;
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
        View view = inflater.inflate(R.layout.search_fragment_layout, container, false);

        searchCardView = view.findViewById(R.id.search_bar_card_view);
        searchRelativeLayout = view.findViewById(R.id.search_relative_layout);
        searchByTextView = view.findViewById(R.id.search_by_text_view);
        searchByTextView.setTypeface(sourceSansProBold);
        searchTextView = view.findViewById(R.id.search_text_view);
        searchTextView.setTypeface(sourceSansProLight);
        searchBarHintTextView = view.findViewById(R.id.search_bar_hint_text_view);
        searchBarHintTextView.setTypeface(sourceSansProLight);

        searchCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent searchIntent = new Intent(getActivity(), SearchActivity.class);
                startActivity(searchIntent);
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        return view;
    }
}
