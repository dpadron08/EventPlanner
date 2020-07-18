package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnSignup;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (ParseUser.getCurrentUser() != null) {
            goMainActivity();
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        linearLayout = findViewById(R.id.linearLayout);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "Login button");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username, password);
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                signupUser(username, password);

            }
        });
    }

    private void signupUser(String username, String password) {
        Log.i(TAG, "Attempting to sign up user: " + username);
        ParseUser user = new ParseUser();
        user.setUsername(username);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Unable to sign in user"+ e.getCode());
                    switch (e.getCode()) {

                        case ParseException.USERNAME_TAKEN:
                            Snackbar.make(linearLayout, "Username is taken", Snackbar.LENGTH_SHORT)
                                    .show();
                            break;
                        default:
                            Snackbar.make(linearLayout, "Error signing up. Please restart app", Snackbar.LENGTH_SHORT)
                                    .show();
                            break;
                    }
                    return;
                }
                goMainActivity();
            }
        });
    }

    private void loginUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with login ", e);
                    // TODO: better error handling

                    switch (e.getCode()) {
                        case ParseException.PASSWORD_MISSING:
                        case ParseException.USERNAME_MISSING:
                            Snackbar.make(linearLayout, "Username or password is missing", Snackbar.LENGTH_SHORT)
                                    .show();
                            break;

                        case ParseException.OBJECT_NOT_FOUND:
                            Snackbar.make(linearLayout, "Invalid username/password", Snackbar.LENGTH_SHORT)
                                    .show();
                            break;
                        case ParseException.USERNAME_TAKEN:
                            Snackbar.make(linearLayout, "Username is taken", Snackbar.LENGTH_SHORT)
                                    .show();
                        default:
                            Snackbar.make(linearLayout, "Error logging in. Please restart app", Snackbar.LENGTH_SHORT)
                                    .show();
                            break;
                    }
                    return;
                }

                goMainActivity();
                Toast.makeText(LoginActivity.this, "Logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void goMainActivity() {
        Intent i  = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }
}