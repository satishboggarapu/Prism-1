package com.mikechoch.prism;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerName;
    private EditText registerUsername;
    private EditText registerEmail;
    private EditText registerPassword;
    private Button registerButton;
    private TextView backToLogin;

    private FirebaseAuth auth;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child(Key.DB_REF_USER_PROFILES);


        registerName = findViewById(R.id.register_name);
        registerUsername = findViewById(R.id.register_username);
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.btn_register);
        backToLogin = findViewById(R.id.login_textview);


        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullName = registerName.getText().toString().trim();
                final String userName = registerUsername.getText().toString().trim();
                final String email = registerEmail.getText().toString().trim();
                String password = registerPassword.getText().toString().trim();
                // todo check if user already exists
                if (areInputsValid(fullName, userName, email, password)) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String uid = auth.getCurrentUser().getUid();
                                DatabaseReference profileReference = databaseReference.child(uid);
                                profileReference.child(Key.DB_REF_USER_PROFILE_FULL_NAME).setValue(fullName);
                                profileReference.child(Key.DB_REF_USER_PROFILE_USERNAME).setValue(userName);

                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finish();
                            }
                        }
                    });
                }
            }
        });


    }

    private boolean areInputsValid(String fullName, String userName, String email, String password) {
        // todo check if email and password are valid
        return true;
    }
}
