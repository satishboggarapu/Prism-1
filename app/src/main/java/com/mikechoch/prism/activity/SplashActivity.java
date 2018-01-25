package com.mikechoch.prism.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.mikechoch.prism.R;

/**
 * Created by mikechoch on 1/21/18.
 */

public class SplashActivity extends AppCompatActivity {

    private ImageView iconImageView;
    private Animation rotateAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity_layout);

        iconImageView = findViewById(R.id.icon_image_view);
        rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.icon_rotate);
        iconImageView.startAnimation(rotateAnimation);

        new IntentLoaderTask().execute();
    }

    /**
     *
     */
    private class IntentLoaderTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Void doInBackground(Void... v) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            boolean isSignedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
            Intent intent = new Intent(SplashActivity.this, isSignedIn ?
                    MainActivity.class : LoginActivity.class);
            int enterAnim = isSignedIn ? R.anim.fade_in : 0;
            int exitAnim = isSignedIn ? R.anim.fade_out : 0;
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(SplashActivity.this, iconImageView, "icon");
            iconImageView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent, options.toBundle());
                    overridePendingTransition(enterAnim, exitAnim);
                    iconImageView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 1000);
                }
            }, 250);
        }
    }
}
