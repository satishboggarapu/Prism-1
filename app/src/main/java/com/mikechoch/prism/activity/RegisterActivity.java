package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.constants.Key;
import com.mikechoch.prism.R;
import com.mikechoch.prism.constants.Message;
import com.mikechoch.prism.helper.Helper;

import java.util.Random;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    /*
     * Globals
     */
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference usersDatabaseRef;

    private Typeface sourceSansProLight;
    private Typeface sourceSansProBold;
    private int screenWidth;
    private int screenHeight;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity_layout);

        // User authentication instance
        auth = FirebaseAuth.getInstance();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_USER_PROFILES);

        // Initialize normal and bold Prism font
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Get the screen width and height of the current phone
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        // Initialize all UI elements
        iconImageView = findViewById(R.id.icon_image_view);
        fullNameTextInputLayout = findViewById(R.id.register_name_text_input_layout);
        fullNameEditText = findViewById(R.id.register_name_edit_text);
        usernameTextInputLayout = findViewById(R.id.register_username_text_input_layout);
        usernameEditText = findViewById(R.id.register_username_edit_text);
        emailTextInputLayout = findViewById(R.id.register_email_text_input_layout);
        emailEditText = findViewById(R.id.register_email_edit_text);
        passwordTextInputLayout = findViewById(R.id.register_password_text_input_layout);
        passwordEditText = findViewById(R.id.register_password_edit_text);
        registerButton = findViewById(R.id.register_submit_button);
        goToLoginButton = findViewById(R.id.login_text_view);
        registerProgressBar = findViewById(R.id.register_progress_bar);

        setupUIElements();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Setup the image view at the top of the Register screen
     * 30% of the screen will be the width and margin the image top by 1/16th of the height
     */
    private void setupIconImageView() {
        iconImageView.getLayoutParams().width = (int) (screenWidth * 0.3);
    }

    /**
     * FullName EditTextLayout Typefaces are set and TextWatcher is setup for error handling
     */
    private void setupFullNameEditText() {
        fullNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (s.length() > 0) {
                            isFullNameValid(s.toString().trim());
                        }
                    }
                }, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /**
     * Username EditTextLayout Typefaces are set and TextWatcher is setup for error handling
     */
    private void setupUsernameEditText() {
        usernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (s.length() > 0) {
                            isUsernameValid(s.toString().trim());
                        }
                    }
                }, 2000);
            }

            @Override
            public void afterTextChanged(Editable e) { }
        });


    }

    /**
     * Email EditTextLayout Typefaces are set and TextWatcher is setup for error handling
     */
    private void setupEmailEditText() {
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (s.length() > 0) {
                            isEmailValid(s.toString().trim());
                        }
                    }
                }, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /**
     * Password EditTextLayout Typefaces are set and TextWatcher is setup for error handling
     */
    private void setupPasswordEditText() {
        passwordTextInputLayout.setPasswordVisibilityToggleEnabled(true);
        passwordTextInputLayout.getPasswordVisibilityToggleDrawable().setTint(Color.WHITE);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (s.length() > 0) {
                            isPasswordValid(s.toString().trim());
                        }
                    }
                }, 2000);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /**
     * Generate a random Default profile picture
     */
    private String generateDefaultProfilePic() {
        // TODO @mike this '10' should be replaced with DefaultProfilePictures.values().length right?c
        return String.valueOf(new Random().nextInt(10));
    }

    /**
     * Setup the register button
     * When all fields are valid the button is enabled
     * When clicked, an attempt to create the account with the entered credentials
     */
    private void setupRegisterButton() {
        registerButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleProgressBar(true);
                final String fullName = getFormattedFullName();
                final String inputUsername = getFormattedUsername();
                final String username = Helper.getFirebaseEncodedUsername(inputUsername);
                final String email = getFormattedEmail();
                final String password = getFormattedPassword();

                if (!areInputsValid(fullName, inputUsername, email, password)) {
                    toggleProgressBar(false);
                    return;
                }

                DatabaseReference accountReference = Default.ACCOUNT_REFERENCE;
                accountReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(username)) {
                            usernameTextInputLayout.setError("Username is taken. Try again");
                            toggleProgressBar(false);
                            return;
                        }
                        // else -> attempt creation of new firebaseUser
                        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = auth.getCurrentUser();
                                    if (user != null) {
                                        UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                                        user.updateProfile(profile);
                                        String uid = user.getUid();
                                        String email = user.getEmail();

                                        DatabaseReference profileReference = usersDatabaseRef.child(uid);
                                        profileReference.child(Key.USER_PROFILE_FULL_NAME).setValue(fullName);
                                        profileReference.child(Key.USER_PROFILE_USERNAME).setValue(username);
                                        profileReference.child(Key.USER_PROFILE_PIC).setValue(generateDefaultProfilePic());

                                        DatabaseReference accountReference = Default.ACCOUNT_REFERENCE.child(username);
                                        accountReference.setValue(email);

                                        intentToMainActivity();
                                    }
                                } else {
                                    toggleProgressBar(false);
                                    Log.e(Default.TAG_DB, Message.USER_ACCOUNT_CREATION_FAIL);
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
                        toggleProgressBar(false);
                        Log.e(Default.TAG_DB, Message.USER_EXIST_CHECK_FAIL, databaseError.toException());
                    }
                });
            }
        });
    }

    /**
     * Setup all UI elements
     */
    private void setupUIElements() {
        // Setup Typefaces for all text based UI elements
        fullNameTextInputLayout.setTypeface(sourceSansProLight);
        fullNameEditText.setTypeface(sourceSansProLight);
        usernameTextInputLayout.setTypeface(sourceSansProLight);
        usernameEditText.setTypeface(sourceSansProLight);
        emailTextInputLayout.setTypeface(sourceSansProLight);
        emailEditText.setTypeface(sourceSansProLight);
        passwordTextInputLayout.setTypeface(sourceSansProLight);
        passwordEditText.setTypeface(sourceSansProLight);
        registerButton.setTypeface(sourceSansProLight);
        goToLoginButton.setTypeface(sourceSansProLight);

        setupIconImageView();
        setupFullNameEditText();
        setupUsernameEditText();
        setupEmailEditText();
        setupPasswordEditText();
        setupRegisterButton();
        setupLoginButton();

    }

    /**
     * Intent to Main Activity from Register Activity
     */
    private void intentToMainActivity() {
        Intent mainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(RegisterActivity.this, iconImageView, "icon");
        iconImageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(mainActivityIntent, options.toBundle());
//                    overridePendingTransition(enterAnim, exitAnim);
                iconImageView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1000);
            }
        }, 250);
    }

    /**
     * Setup the button to take you back to Login Activity
     */
    private void setupLoginButton() {
        goToLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterActivity.super.onBackPressed();
            }
        });
    }

    /**
     * Toggles the register button and login button to hide and shows the progress bar spinner
     */
    private void toggleProgressBar(boolean showProgressBar) {
        int progressVisibility = showProgressBar ? View.VISIBLE : View.INVISIBLE;
        int buttonVisibility = showProgressBar ? View.INVISIBLE : View.VISIBLE;
        registerButton.setVisibility(buttonVisibility);
        goToLoginButton.setVisibility(buttonVisibility);
        registerProgressBar.setVisibility(progressVisibility);
    }

    /**
     * Verify that all inputs to the EditText fields are valid
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
     * @return: runs the current fullName through several checks to verify it is valid
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
            fullNameTextInputLayout.setError("Name can only contain letters, space, and apostrophe");
            return false;
        }
        if (Pattern.matches(".*(.)\\1{3,}.*", fullName)) {
            fullNameTextInputLayout.setError("Name cannot contain more than 3 repeating characters");
            return false;
        }
        if (Pattern.matches(".*(['])\\1{1,}.*", fullName)) {
            fullNameTextInputLayout.setError("Name cannot contain more than 1 apostrophe");
            return false;
        }
        if (!Character.isAlphabetic(fullName.charAt(0))) {
            fullNameTextInputLayout.setError("Name must start with a letter");
            return false;
        }
        if (fullName.endsWith("'")) {
            fullNameTextInputLayout.setError("Name must end with a letter");
            return false;
        }
        fullNameTextInputLayout.setErrorEnabled(false);
        return true;
    }

    /**
     * @param username
     * @return: runs the current username through several checks to verify it is valid
     */
    private boolean isUsernameValid(String username) {
        if (username.length() < 5) {
            usernameTextInputLayout.setError("Username must be as least 5 characters long");
            return false;
        }
        if (username.length() > 30) {
            usernameTextInputLayout.setError("Username cannot be longer than 30 characters");
            return false;
        }
        if (!Pattern.matches("^[a-z0-9._']+", username)) {
            usernameTextInputLayout.setError("Username can only contain lowercase letters, numbers, period, and underscore");
            return false;
        }
        if (Pattern.matches(".*([a-z0-9])\\1{5,}.*", username)) {
            usernameTextInputLayout.setError("Username cannot contain more than 3 repeating characters");
            return false;
        }
        if (Pattern.matches(".*([._]){2,}.*", username)) {
            usernameTextInputLayout.setError("Username cannot contain more than 1 repeating symbol");
            return false;
        }
        if (!Character.isAlphabetic(username.charAt(0))) {
            usernameTextInputLayout.setError("Username must start with a letter");
            return false;
        }
        if (username.endsWith("_") || username.endsWith(".")) {
            usernameTextInputLayout.setError("Username must end with a letter or number");
            return false;
        }
        usernameTextInputLayout.setErrorEnabled(false);
        return true;

    }

    /**
     * @param email
     * @return: runs the current email through several checks to verify it is valid
     */
    private boolean isEmailValid(String email) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailTextInputLayout.setErrorEnabled(false);
            return true;
        } else {
            emailTextInputLayout.setError("Invalid email");
            return false;
        }
    }

    /**
     * @param password
     * @return: runs the current password through several checks to verify it is valid
     */
    private boolean isPasswordValid(String password) {
        // TODO: Add more checks for valid password?
        if (password.length() > 5) {
            passwordTextInputLayout.setErrorEnabled(false);
            return true;
        } else {
            passwordTextInputLayout.setError("Password must be at least 6 characters long");
            return false;
        }
    }

    /**
     * Cleans the fullName entered and returns the clean version
     */
    public String getFormattedFullName() {
        return fullNameEditText.getText().toString().trim().replaceAll(" +", " ");

    }

    /**
     * Cleans the username entered and returns the clean version
     */
    public String getFormattedUsername() {
        return usernameEditText.getText().toString().trim().toLowerCase();
    }


    /**
     * Cleans the email entered and returns the clean version
     */
    public String getFormattedEmail() {
        return emailEditText.getText().toString().trim().toLowerCase();
    }

    /**
     * Cleans the password entered and returns the clean version
     */
    public String getFormattedPassword() {
        return passwordEditText.getText().toString().trim();
    }
}
