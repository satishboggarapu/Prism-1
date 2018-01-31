package com.mikechoch.prism.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.mikechoch.prism.PrismUser;
import com.mikechoch.prism.R;
import com.mikechoch.prism.UsersRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * Created by mikechoch on 1/30/18.
 */

public class UsersActivity extends AppCompatActivity {

    /*
     * Global variables
     */
    private Toolbar toolbar;

    private RecyclerView usersRecyclerView;
    private UsersRecyclerViewAdapter usersRecyclerViewAdapter;

    private ArrayList<PrismUser> prismUserArrayList;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.context_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            default:
                break;
        }
        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_activity_layout);

        Intent intent = getIntent();
        String toolbarTitle = intent.getStringExtra("LikeRepostTitle");

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(toolbarTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prismUserArrayList = new ArrayList<>();

        // TODO: populate users ArrayList

        usersRecyclerView = findViewById(R.id.users_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(this.getResources().getDrawable(R.drawable.recycler_view_divider));
        usersRecyclerView.setLayoutManager(linearLayoutManager);
        usersRecyclerView.setItemAnimator(defaultItemAnimator);
        usersRecyclerView.addItemDecoration(dividerItemDecoration);

        usersRecyclerViewAdapter = new UsersRecyclerViewAdapter(this, prismUserArrayList);
        usersRecyclerView.setAdapter(usersRecyclerViewAdapter);

    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    /**
     * 
     */
    private class LikeRepostTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
        }

    }
}
