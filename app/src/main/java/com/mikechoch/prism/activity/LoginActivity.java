package com.mikechoch.prism.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
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
import com.mikechoch.prism.constants.Message;

public class LoginActivity extends AppCompatActivity {

    /*
     * Globals
     */
    private FirebaseAuth auth;

    private ImageView iconImageView;
    private TextInputLayout emailTextInputLayout;
    private EditText emailOrUsernameEditText;
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
        emailOrUsernameEditText = findViewById(R.id.email_edit_text);
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
        emailOrUsernameEditText.setTypeface(sourceSansProLight);
        emailOrUsernameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                emailTextInputLayout.setErrorEnabled(false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (s.length() > 0) {
                            isEmailOrUsernameValid(s.toString().trim());
                        }
                    }
                }, 2000);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
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
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                passwordTextInputLayout.setErrorEnabled(false);
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
            public void afterTextChanged(Editable editable) { }
        });
    }

    /**
     * Login button is disabled, formatting is set, and OnClickListener is setup
     * When the login button is clicked, this should check whether it is a username or email
     * Error handle for invalid credentials and otherwise go to MainActivity
     */
    private void setupLoginButton() {
        loginButton.setTypeface(sourceSansProLight);
        loginButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleLoginProgressBar(true);
                String emailOrUsername = getFormattedEmailOrUsername();
                String password = getFormattedPassword();

                if (!isEmailOrUsernameValid(emailOrUsername) || !isPasswordValid(password)) {
                    toggleLoginProgressBar(false);
                    return;
                }

                // If input is username, extract email from database
                if (!isInputOfTypeEmail(emailOrUsername)) {
                    DatabaseReference accountReference = Default.ACCOUNT_REFERENCE.child(emailOrUsername);
                    accountReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String email = (String) dataSnapshot.getValue();
                                attemptLogin(email, password);
                            } else {
                                emailTextInputLayout.setError("Username not found");
                                toggleLoginProgressBar(false);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.wtf(Default.TAG_DB, Message.USER_EXIST_CHECK_FAIL, databaseError.toException());
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

    private String getFormattedPassword() {
        return passwordEditText.getText().toString().trim();
    }

    private String getFormattedEmailOrUsername() {
        return emailOrUsernameEditText.getText().toString().trim();
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
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    intentToMainActivity();
                } else {
                    passwordTextInputLayout.setError("Invalid email/username or password");
                    toggleLoginProgressBar(false);
                    Log.i(Default.TAG_DB, Message.LOGIN_ATTEMPT_FAIL, task.getException());
                }
            }
        });
    }

    /**
     * Toggles the login button and register button to hide and shows the progress bar spinner
     */
    private void toggleLoginProgressBar(boolean showProgressBar) {
        int progressVisibility = showProgressBar ? View.VISIBLE : View.INVISIBLE;
        int buttonVisibility = showProgressBar ? View.INVISIBLE : View.VISIBLE;
        loginButton.setVisibility(buttonVisibility);
        goToRegisterButton.setVisibility(buttonVisibility);
        loginProgressBar.setVisibility(progressVisibility);
    }

    /**
     * Email/Username validation check
     */
    private boolean isEmailOrUsernameValid(String email) {
        boolean isValid = (Patterns.EMAIL_ADDRESS.matcher(email).matches()) ||
                (email.length() >=  5 && email.length() <= 30);
        if (!isValid) {
           emailTextInputLayout.setError("Invalid username/email");
        }
        return isValid;
    }

    /**
     * Password validation check
     */
    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /**
     * Checks to see if what user typed in the username/email editText
     * is of type email or username. The purpose is that if the user
     * enters an email, we can directly attemptLogin otherwise for username,
     * we have to go to the database and extract the email for the given
     * username
     * @param emailOrUsername text from the email/username editText
     * @return True if input is an email and False if it's a username
     */
    private boolean isInputOfTypeEmail(String emailOrUsername) {
        return Patterns.EMAIL_ADDRESS.matcher(emailOrUsername).matches();
    }


    /**
     * Shortcut for toasting a message
     */
    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
