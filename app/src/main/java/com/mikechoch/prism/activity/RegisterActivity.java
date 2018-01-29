package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
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
import com.mikechoch.prism.Default;
import com.mikechoch.prism.Key;
import com.mikechoch.prism.R;

import java.util.regex.Pattern;

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

    private DatabaseReference usersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity_layout);

        auth = FirebaseAuth.getInstance();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_USER_PROFILES);

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
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(userName).build();
                                user.updateProfile(profile);
                                String uid = user.getUid();
                                String email = user.getEmail();

                                DatabaseReference profileReference = usersDatabaseRef.child(uid);
                                profileReference.child(Key.DB_REF_USER_PROFILE_FULL_NAME).setValue(fullName);
                                profileReference.child(Key.DB_REF_USER_PROFILE_USERNAME).setValue(userName);

                                DatabaseReference accountReference = Default.ACCOUNT_REFERENCE.child(userName);
                                accountReference.setValue(email);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     *
     */
    private boolean areInputsValid(String fullName, String username, String email, String password) {
        return (isFullNameValid(fullName) &&
                isUsernameValid(username) &&
                isEmailValid(email) &&
                isPasswordValid(password));
    }

    /**
     *
     * @param fullName
     * @return
     */
    private boolean isFullNameValid(String fullName) {
        if (fullName.length() < 2) {
            registerNameTextInputLayout.setError("Name must be at least 2 characters long");
            return false;
        }
        if (fullName.length() > 70) {
            registerNameTextInputLayout.setError("Name cannot be longer than 70 characters");
            return false;
        }
        if (!Pattern.matches("^[a-zA-Z ']+", fullName)) {
            registerNameTextInputLayout.setError("Name can only have alphabets, space and apostrophe");
            return false;
        }
        if (Pattern.matches(".*(.)\\1{3,}.*", fullName)) {
            registerNameTextInputLayout.setError("Name cannot have more than 3 repeating characters");
            return false;
        }
        if (Pattern.matches(".*(['])\\1{1,}.*", fullName)) {
            registerNameTextInputLayout.setError("Name cannot have more than 1 apostrophe");
            return false;
        }
        if (!Character.isAlphabetic(fullName.charAt(0))) {
            registerNameTextInputLayout.setError("Name must start with a letter");
            return false;
        }
        return true;
    }

    /**
     *
     * @param username
     * @return
     */
    private boolean isUsernameValid(String username) {
        if (username.length() < 5) {
            // TODO: show error "Username must be as least 5 characters long"
            return false;
        }
        if (username.length() > 30) {
            // TODO: show error "Username cannot be longer than 30 characters"
            return false;
        }
        if (!Pattern.matches("^[a-z0-9._']+", username)) {
            // TODO: show error "Username can only contain lowercase letters, numbers, period and underscore"
            return false;
        }
        if (Pattern.matches(".*([a-z0-9])\\1{5,}.*", username)) {
            // TODO: show error "Username cannot have more than 3 repeating characters"
            return false;
        }
        if (Pattern.matches(".*([._]){2,}.*", username)) {
            // TODO: show error "Username cannot have more than 1 repeating symbol"
            return false;
        }
        if (!Character.isAlphabetic(username.charAt(0))) {
            // TODO: show error "Username must start with a letter"
            return false;
        }
        return true;

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
