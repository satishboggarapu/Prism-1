package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mikechoch.prism.Key;
import com.mikechoch.prism.R;
import com.mikechoch.prism.helper.TransitionUtils;

public class RegisterActivity extends AppCompatActivity {

    private ImageView iconImageView;
    private TextInputLayout registerNameTextInputLayout;
    private EditText registerNameEditText;
    private TextInputLayout registerUsernameTextInputLayout;
    private EditText registerUsernameEditText;
    private TextInputLayout registerEmailTextInputLayout;
    private EditText registerEmailEditText;
    private TextInputLayout registerPasswordTextInputLayout;
    private EditText registerPasswordEditText;
    private Button registerButton;
    private TextView goToLoginButton;
    private ProgressBar registerProgressBar;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity_layout);

        getWindow().setEnterTransition(TransitionUtils.makeEnterTransition());

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_USER_PROFILES);

        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        iconImageView = findViewById(R.id.icon_image_view);
        iconImageView.getLayoutParams().width = (int) (screenWidth * 0.3);

        registerNameTextInputLayout = findViewById(R.id.register_name_text_input_layout);
        registerNameTextInputLayout.setTypeface(sourceSansProLight);
        registerNameEditText = findViewById(R.id.register_name_edit_text);
        registerNameEditText.setTypeface(sourceSansProLight);
        registerNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                registerNameTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = areInputsValid(registerNameEditText.getText().toString().trim(),
                        registerUsernameEditText.getText().toString().trim(),
                        registerEmailEditText.getText().toString().trim(),
                        registerPasswordEditText.getText().toString().trim());
                registerButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                registerButton.setBackgroundTintList(getResources().getColorStateList(color));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        registerUsernameTextInputLayout = findViewById(R.id.register_username_text_input_layout);
        registerUsernameTextInputLayout.setTypeface(sourceSansProLight);
        registerUsernameEditText = findViewById(R.id.register_username_edit_text);
        registerUsernameEditText.setTypeface(sourceSansProLight);
        registerUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                registerUsernameTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = areInputsValid(registerNameEditText.getText().toString().trim(),
                        registerUsernameEditText.getText().toString().trim(),
                        registerEmailEditText.getText().toString().trim(),
                        registerPasswordEditText.getText().toString().trim());
                registerButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                registerButton.setBackgroundTintList(getResources().getColorStateList(color));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        registerEmailTextInputLayout = findViewById(R.id.register_email_text_input_layout);
        registerEmailTextInputLayout.setTypeface(sourceSansProLight);
        registerEmailEditText = findViewById(R.id.register_email_edit_text);
        registerEmailEditText.setTypeface(sourceSansProLight);
        registerEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                registerEmailTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = areInputsValid(registerNameEditText.getText().toString().trim(),
                        registerUsernameEditText.getText().toString().trim(),
                        registerEmailEditText.getText().toString().trim(),
                        registerPasswordEditText.getText().toString().trim());
                registerButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                registerButton.setBackgroundTintList(getResources().getColorStateList(color));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        registerPasswordTextInputLayout = findViewById(R.id.register_password_text_input_layout);
        registerPasswordTextInputLayout.setTypeface(sourceSansProLight);
        registerPasswordEditText = findViewById(R.id.register_password_edit_text);
        registerPasswordEditText.setTypeface(sourceSansProLight);
        registerPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                registerPasswordTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = areInputsValid(registerNameEditText.getText().toString().trim(),
                        registerUsernameEditText.getText().toString().trim(),
                        registerEmailEditText.getText().toString().trim(),
                        registerPasswordEditText.getText().toString().trim());
                registerButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                registerButton.setBackgroundTintList(getResources().getColorStateList(color));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        registerButton = findViewById(R.id.submit_button);
        registerButton.setEnabled(false);
        registerButton.setBackgroundTintList(getResources().getColorStateList(R.color.disabledButtonColor));
        registerButton.setTypeface(sourceSansProLight);

        goToLoginButton = findViewById(R.id.login_text_view);
        goToLoginButton.setTypeface(sourceSansProLight);

        registerProgressBar = findViewById(R.id.register_progress_bar);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = registerNameEditText.getText().toString().trim();
                final String userName = registerUsernameEditText.getText().toString().trim();
                final String email = registerEmailEditText.getText().toString().trim();
                String password = registerPasswordEditText.getText().toString().trim();

                registerButton.setVisibility(View.INVISIBLE);
                goToLoginButton.setVisibility(View.INVISIBLE);
                registerProgressBar.setVisibility(View.VISIBLE);

                // todo check if user already exists
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();
                            DatabaseReference profileReference = databaseReference.child(uid);
                            profileReference.child(Key.DB_REF_USER_PROFILE_FULL_NAME).setValue(fullName);
                            profileReference.child(Key.DB_REF_USER_PROFILE_USERNAME).setValue(userName);
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(userName).build();
                                user.updateProfile(profile);
                            }

                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        } else {
                            registerButton.setVisibility(View.VISIBLE);
                            goToLoginButton.setVisibility(View.VISIBLE);
                            registerProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });



            }
        });

        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.super.onBackPressed();
            }
        });
    }

    /**
     *
     */
    private boolean areInputsValid(String fullName, String username, String email, String password) {
        return (isNameValid(fullName) &&
                isUsernameValid(username) &&
                isEmailValid(email) &&
                isPasswordValid(password));
    }

    /**
     *
     */
    private boolean isNameValid(String fullName) {
        // TODO: Add more checks for valid fullName?
        if (fullName.length() > 3) {
            return true;
        }
        return false;
    }

    /**
     *
     */
    private boolean isUsernameValid(String username) {
        // TODO: Add more checks for valid username?
        if (username.length() > 5) {
            return true;
        }
        return false;
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
}
