package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikechoch.prism.constants.Default;
import com.mikechoch.prism.R;

public class LoginActivity extends AppCompatActivity {

    /*
     * Globals
     */
    private FirebaseAuth auth;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_layout);

        // User authentication instance
        auth = FirebaseAuth.getInstance();

        // Initialize normal and bold Prism font
        sourceSansProLight = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Light.ttf");
        sourceSansProBold = Typeface.createFromAsset(getAssets(), "fonts/SourceSansPro-Black.ttf");

        // Get the screen width and height of the current phone
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        // Initialize all UI elements for Login Activity
        iconImageView = findViewById(R.id.icon_image_view);
        emailTextInputLayout = findViewById(R.id.email_text_input_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordTextInputLayout = findViewById(R.id.password_text_input_layout);
        passwordEditText = findViewById(R.id.password_edit_text);
        loginButton = findViewById(R.id.submit_button);
        goToRegisterButton = findViewById(R.id.register_text_view);
        loginProgressBar = findViewById(R.id.login_progress_bar);

        // Setup the image view at the top of the Login screen
        // 50% of the screen will be the width and margin the image top by 1/16th of the height
        iconImageView.getLayoutParams().width = (int) (screenWidth * 0.5);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) iconImageView.getLayoutParams();
        lp.setMargins(0, (screenHeight/16), 0, 0);
        iconImageView.setLayoutParams(lp);

        // Setup all UI elements
        setupEmailEditText();
        setupPasswordEditText();
        setupLoginButton();
        setupGoToRegisterButton();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Email EditTextLayout Typefaces are set and TextWatcher is setup for error handling
     */
    private void setupEmailEditText() {
        emailTextInputLayout.setTypeface(sourceSansProLight);
        emailEditText.setTypeface(sourceSansProLight);
        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = isEmailOrUsernameValid(emailEditText.getText().toString().trim()) &&
                        isPasswordValid(passwordEditText.getText().toString().trim());
                loginButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                loginButton.setBackgroundTintList(getResources().getColorStateList(color));            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Password EditTextLayout Typefaces are set and TextWatcher is setup for error handling
     */
    private void setupPasswordEditText() {
        passwordTextInputLayout.setTypeface(sourceSansProLight);
        passwordTextInputLayout.setPasswordVisibilityToggleEnabled(true);
        passwordTextInputLayout.getPasswordVisibilityToggleDrawable().setTint(Color.WHITE);
        passwordEditText.setTypeface(sourceSansProLight);
        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordTextInputLayout.setErrorEnabled(false);
                boolean isEmailAndPasswordValid = isEmailOrUsernameValid(emailEditText.getText().toString().trim()) &&
                        isPasswordValid(passwordEditText.getText().toString().trim());
                loginButton.setEnabled(isEmailAndPasswordValid);
                int color = isEmailAndPasswordValid ? R.color.colorAccent : R.color.disabledButtonColor;
                loginButton.setBackgroundTintList(getResources().getColorStateList(color));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Login button is disabled, formatting is set, and OnClickListener is setup
     * When the login button is clicked, this should check whether it is a username or email
     * Error handle for invalid credentials and otherwise go to MainActivity
     */
    private void setupLoginButton() {
        loginButton.setEnabled(false);
        loginButton.setTypeface(sourceSansProLight);
        loginButton.setBackgroundTintList(getResources().getColorStateList(R.color.disabledButtonColor));
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLoginProgressBar(true);
                String emailOrUsername = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // If input is username, extract email from database
                if (!emailOrUsername.contains("@")) {
                    DatabaseReference accountReference = Default.ACCOUNT_REFERENCE.child(emailOrUsername);
                    accountReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String email = (String) dataSnapshot.getValue();
                                attemptLogin(email, password);
                            } else {
                                toast("Could not find account with your username");
                                toggleLoginProgressBar(false);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.wtf("Database Error", databaseError.getDetails());
                            toast("An error occurred logging in");
                            toggleLoginProgressBar(false);
                        }
                    });
                } else {
                    attemptLogin(emailOrUsername, password);
                }


            }
        });
    }

    /**
     * Setup the register button TextView to go to RegisterActivity when clicked
     */
    private void setupGoToRegisterButton() {
        goToRegisterButton.setTypeface(sourceSansProLight);
        goToRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentToRegisterActivity();
            }
        });
    }

    /**
     * Intent to Register Activity from Login Activity
     */
    private void intentToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        Pair<View, String> iconPair = Pair.create(iconImageView, "icon");
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(LoginActivity.this, iconPair);
        startActivity(registerIntent, options.toBundle());
        overridePendingTransition(0, 0);
    }

    /**
     * Intent to Main Activity from Login Activity
     */
    private void intentToMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * Perform validation checks before attempting sign in
     * Also display a loading spinner until onComplete
     */
    private void attemptLogin(String email, String password) {
        // todo perform validation checks before attempting sign in
        // todo also display a loading spinner until onComplete
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    intentToMainActivity();
                } else {
                    passwordTextInputLayout.setError("Invalid email/username or password");
                    toggleLoginProgressBar(false);
                }
            }
        });
    }

    /**
     * Toggles the login button and register button to hide and shows the progress bar spinner
     */
    private void toggleLoginProgressBar(boolean isLoginAttempt) {
        int progressVisibility = isLoginAttempt ? View.VISIBLE : View.INVISIBLE;
        int buttonVisibility = isLoginAttempt ? View.INVISIBLE : View.VISIBLE;
        loginButton.setVisibility(buttonVisibility);
        goToRegisterButton.setVisibility(buttonVisibility);
        loginProgressBar.setVisibility(progressVisibility);
    }

    /**
     * Email/Username validation check
     */
    private boolean isEmailOrUsernameValid(String email) {
        // TODO: Add more checks for valid email?
        return true;
    }

    /**
     * Password validation check
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
