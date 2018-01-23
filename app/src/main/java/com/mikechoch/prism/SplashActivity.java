package com.mikechoch.prism;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;

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
            Intent intent = new Intent(SplashActivity.this,
                    FirebaseAuth.getInstance().getCurrentUser() == null ?
                            LoginActivity.class : MainActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
