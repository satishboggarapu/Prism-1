package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mikechoch.prism.R;
import com.mikechoch.prism.helper.TransitionUtils;

public class LoginActivity extends AppCompatActivity {

    private ImageView iconImageView;
    private TextInputLayout emailTextInputLayout;
    private EditText emailEditText;
    private TextInputLayout passwordTextInputLayout;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView goToRegisterButton;
    private ProgressBar loginProgressBar;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        getWindow().setEnterTransition(TransitionUtils.makeEnterTransition());

        auth = FirebaseAuth.getInstance();

        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        iconImageView = findViewById(R.id.icon_image_view);
        iconImageView.getLayoutParams().width = (int) (screenWidth * 0.5);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) iconImageView.getLayoutParams();
        lp.setMargins(0, (screenHeight/16), 0, 0);
        iconImageView.setLayoutParams(lp);

        emailTextInputLayout = findViewById(R.id.email_text_input_layout);
        emailTextInputLayout.setTypeface(sourceSansProLight);
        emailEditText = findViewById(R.id.email_edit_text);
        emailEditText.setTypeface(sourceSansProLight);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = isEmailValid(emailEditText.getText().toString().trim()) &&
                        isPasswordValid(passwordEditText.getText().toString().trim());
                loginButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                loginButton.setBackgroundTintList(getResources().getColorStateList(color));            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        passwordTextInputLayout = findViewById(R.id.password_text_input_layout);
        passwordTextInputLayout.setTypeface(sourceSansProLight);
        passwordEditText = findViewById(R.id.password_edit_text);
        passwordEditText.setTypeface(sourceSansProLight);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = isEmailValid(emailEditText.getText().toString().trim()) &&
                        isPasswordValid(passwordEditText.getText().toString().trim());
                loginButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                loginButton.setBackgroundTintList(getResources().getColorStateList(color));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        loginButton = findViewById(R.id.submit_button);
        loginButton.setEnabled(false);
        loginButton.setBackgroundTintList(getResources().getColorStateList(R.color.disabledButtonColor));
        loginButton.setTypeface(sourceSansProLight);

        goToRegisterButton = findViewById(R.id.register_text_view);
        goToRegisterButton.setTypeface(sourceSansProLight);

        loginProgressBar = findViewById(R.id.login_progress_bar);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                loginButton.setVisibility(View.INVISIBLE);
                goToRegisterButton.setVisibility(View.INVISIBLE);
                loginProgressBar.setVisibility(View.VISIBLE);

                // todo perform validation checks before attempting sign in
                // todo also display a loading spinner until onComplete

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        } else {
                            passwordTextInputLayout.setError("Invalid email or password");
                            loginButton.setVisibility(View.VISIBLE);
                            goToRegisterButton.setVisibility(View.VISIBLE);
                            loginProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });

        goToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);

                Pair<View, String> iconPair = Pair.create(iconImageView, "icon");
                Pair<View, String> submitButtonPair = Pair.create(loginButton, "submit_button");

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(LoginActivity.this, iconPair, submitButtonPair);
                startActivity(registerIntent, options.toBundle());
                overridePendingTransition(0, 0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     *
     */
    private boolean isEmailValid(String email) {
        // TODO: Add more checks for valid email?
        if (email.contains("@")) {
            return true;
        }
        return false;
    }

    /**
     *
     */
    private boolean isPasswordValid(String password) {
        // TODO: Add more checks for valid password?
        if (password.length() > 5) {
            return true;
        }
        return false;
    }

    /**
     * Shortcut for toasting a message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
