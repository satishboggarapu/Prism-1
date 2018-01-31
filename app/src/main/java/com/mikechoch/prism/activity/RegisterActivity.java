package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Patterns;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.Default;
import com.mikechoch.prism.Key;
import com.mikechoch.prism.R;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private ImageView iconImageView;
    private TextInputLayout fullNameTextInputLayout;
    private EditText fullNameEditText;
    private TextInputLayout usernameTextInputLayout;
    private EditText usernameEditText;
    private TextInputLayout emailTextInputLayout;
    private EditText emailEditText;
    private TextInputLayout passwordTextInputLayout;
    private EditText passwordEditText;
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
        registerProgressBar = findViewById(R.id.register_progress_bar);
        goToLoginButton = findViewById(R.id.login_text_view);
        goToLoginButton.setTypeface(sourceSansProLight);
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.super.onBackPressed();
            }
        });

        setupFullNameEditText();
        setupUsernameEditText();
        setupEmailEditText();
        setupPasswordEditText();
        setupRegisterButton();

    }

    private void setupFullNameEditText() {
        fullNameTextInputLayout = findViewById(R.id.register_name_text_input_layout);
        fullNameTextInputLayout.setTypeface(sourceSansProLight);
        fullNameEditText = findViewById(R.id.register_name_edit_text);
        fullNameEditText.setTypeface(sourceSansProLight);
        fullNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                fullNameTextInputLayout.setError(null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (s.length() > 0) {
                                isFullNameValid(s.toString().trim());
                            }
                        }
                    }, 3000);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupUsernameEditText() {
        usernameTextInputLayout = findViewById(R.id.register_username_text_input_layout);
        usernameTextInputLayout.setTypeface(sourceSansProLight);
        usernameEditText = findViewById(R.id.register_username_edit_text);
        usernameEditText.setFilters(new InputFilter[] {
                new InputFilter.AllCaps() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        return String.valueOf(source).toLowerCase();
                    }
                }
        });
        usernameEditText.setTypeface(sourceSansProLight);
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                usernameTextInputLayout.setError(null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (s.length() > 0) {
                                isUsernameValid(s.toString().trim());
                            }
                        }
                    }, 3000);
            }

            @Override
            public void afterTextChanged(Editable e) { }
        });
    }

    private void setupEmailEditText() {
        emailTextInputLayout = findViewById(R.id.register_email_text_input_layout);
        emailTextInputLayout.setTypeface(sourceSansProLight);
        emailEditText = findViewById(R.id.register_email_edit_text);
        emailEditText.setTypeface(sourceSansProLight);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailTextInputLayout.setError(null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (s.length() > 0) {
                                isEmailValid(s.toString().trim());
                            }
                        }
                    }, 3000);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupPasswordEditText() {
        passwordTextInputLayout = findViewById(R.id.register_password_text_input_layout);
        passwordTextInputLayout.setTypeface(sourceSansProLight);
        passwordTextInputLayout.setPasswordVisibilityToggleEnabled(true);
        passwordTextInputLayout.getPasswordVisibilityToggleDrawable().setTint(Color.WHITE);
                passwordEditText = findViewById(R.id.register_password_edit_text);
        passwordEditText.setTypeface(sourceSansProLight);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordTextInputLayout.setError(null);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (s.length() > 0) {
                                isPasswordValid(s.toString().trim());
                            }
                        }
                    }, 3000);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupRegisterButton() {
        registerButton = findViewById(R.id.submit_button);
        registerButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        registerButton.setTypeface(sourceSansProLight);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleProgressBar(true);
                final String fullName = getFormattedFullName();
                final String userName = getFormattedUsername();
                final String email = getFormattedEmail();
                final String password = getFormattedPassword();

                if (!areInputsValid(fullName, userName, email, password)) {
                    toggleProgressBar(false);
                    return;
                }

                DatabaseReference accountReference = Default.ACCOUNT_REFERENCE;
                accountReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(userName)) {
                            toggleProgressBar(false);
                            usernameTextInputLayout.setError("Username is taken. Try again");
                            return;
                        }
                        // else -> attempt creation of new user
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
                                    toggleProgressBar(false);
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthWeakPasswordException weakPassword) {
                                        passwordTextInputLayout.setError("Password is too weak");
                                    } catch (FirebaseAuthInvalidCredentialsException invalidEmail) {
                                        emailTextInputLayout.setError("Invalid email");
                                    } catch (FirebaseAuthUserCollisionException existEmail) {
                                        emailTextInputLayout.setError("Email already exists");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // TODO Log error
                    }
                });


            }
        });
    }

    private void toggleProgressBar(boolean showProgressBar) {
        int progressVisibility = showProgressBar ? View.VISIBLE : View.INVISIBLE;
        int buttonVisibility = showProgressBar ? View.INVISIBLE : View.VISIBLE;
        registerButton.setVisibility(buttonVisibility);
        goToLoginButton.setVisibility(buttonVisibility);
        registerProgressBar.setVisibility(progressVisibility);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     *
     */
    private boolean areInputsValid(String fullName, String username, String email, String password) {
        boolean isFullNameValid = isFullNameValid(fullName);
        boolean isUsernameValid = isUsernameValid(username);
        boolean isEmailValid = isEmailValid(email);
        boolean isPasswordValid = isPasswordValid(password);
        return isFullNameValid && isUsernameValid && isEmailValid && isPasswordValid;
    }

    /**
     * @param fullName
     * @return
     */
    private boolean isFullNameValid(String fullName) {
        if (fullName.length() < 2) {
            fullNameTextInputLayout.setError("Name must be at least 2 characters long");
            return false;
        }
        if (fullName.length() > 70) {
            fullNameTextInputLayout.setError("Name cannot be longer than 70 characters");
            return false;
        }
        if (!Pattern.matches("^[a-zA-Z ']+", fullName)) {
            fullNameTextInputLayout.setError("Name can only have alphabets, space and apostrophe");
            return false;
        }
        if (Pattern.matches(".*(.)\\1{3,}.*", fullName)) {
            fullNameTextInputLayout.setError("Name cannot have more than 3 repeating characters");
            return false;
        }
        if (Pattern.matches(".*(['])\\1{1,}.*", fullName)) {
            fullNameTextInputLayout.setError("Name cannot have more than 1 apostrophe");
            return false;
        }
        if (!Character.isAlphabetic(fullName.charAt(0))) {
            fullNameTextInputLayout.setError("Name must start with a letter");
            return false;
        }
        return true;
    }

    /**
     * @param username
     * @return
     */
    private boolean isUsernameValid(String username) {
        if (username.length() < 5) {
            usernameTextInputLayout.setError("Username must be as least 5 characters long");
            return false;
        }
        if (username.length() > 30) {
            // TODO: show error
            usernameTextInputLayout.setError("Username cannot be longer than 30 characters");
            return false;
        }
        if (!Pattern.matches("^[a-z0-9._']+", username)) {
            usernameTextInputLayout.setError("Username can only contain letters, numbers, period and underscore");
            return false;
        }
        if (Pattern.matches(".*([a-z0-9])\\1{5,}.*", username)) {
            usernameTextInputLayout.setError("Username cannot have more than 3 repeating characters");
            return false;
        }
        if (Pattern.matches(".*([._]){2,}.*", username)) {
            usernameTextInputLayout.setError("Username cannot have more than 1 repeating symbol");
            return false;
        }
        if (!Character.isAlphabetic(username.charAt(0))) {
            usernameTextInputLayout.setError("Username must start with a letter");
            return false;
        }
        return true;

    }

    /**
     *
     */
    private boolean isEmailValid(String email) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        } else {
            emailTextInputLayout.setError("Invalid email");
            return false;
        }
    }

    /**
     *
     */
    private boolean isPasswordValid(String password) {
        // TODO: Add more checks for valid password?
        if (password.length() > 5) {
            return true;
        } else {
            passwordTextInputLayout.setError("Password must be at least 6 characters long");
            return false;
        }
    }

    public String getFormattedFullName() {
        return fullNameEditText.getText().toString().trim().replaceAll(" +", " ");

    }

    public String getFormattedUsername() {
        return usernameEditText.getText().toString().trim().toLowerCase();
    }

    public String getFormattedEmail() {
        return emailEditText.getText().toString().trim().toLowerCase();
    }

    public String getFormattedPassword() {
        return passwordEditText.getText().toString().trim();
    }
}
