package com.notesapp.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener{
    //Creating instance variables
    //This is the TextView that hyperlinks to the SignUpActivity
    private TextView textViewSignIn;
    //The button that will be clicked when the user wants to login
    private Button buttonLogin;
    //These are the TextInputEditTexts which hold he username and password
    private TextInputEditText textInputEditTextUsername, textInputEditTextPassword;

    //onCreate()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        //trying to check if the user has already logged in. If yes then transition to LoggedInActivity
        try
        {
            if (ParseUser.getCurrentUser() != null)
            {
                //Transitioning to LoggedInActivity and destroying the current activity
                Intent intent = new Intent(LogInActivity.this, LoggedInActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Initializing the instance variables
        textViewSignIn = findViewById(R.id.textViewSignIn);
        buttonLogin = findViewById(R.id.buttonLogin);
        textInputEditTextUsername = findViewById(R.id.textInputEditTextUsername);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        //Setting the OnClickListeners
        textViewSignIn.setOnClickListener(this);
        buttonLogin.setOnClickListener(this);
    }

    //onClick() method
    @Override
    public void onClick(View v) {
        //Switching between blocks of code based on the Item that is clicked
        switch (v.getId())
        {
            //Codes for logging in
            case R.id.buttonLogin :
                logIn();
                break;
            //If user clicks the Sign Up hyperlink
            case R.id.textViewSignIn :
                //Transitioning to SignUpActivity and destroying the current activity
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    //logIn() method is that method that logs in the user
    private void logIn()
    {
        //Checking if username field is not empty
        if (!textInputEditTextUsername.getText().toString().isEmpty())
        {
            //Checking if password field is not empty
            if (!textInputEditTextPassword.getText().toString().isEmpty())
            {
                //Logging in the user
                ParseUser.logInInBackground(textInputEditTextUsername.getText().toString(), textInputEditTextPassword.getText().toString(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) { //user means human error and e means eg combination error of username and password
                        //If no errors and user exist
                        if (user != null && e == null)
                        {
                            //Transitioning to LoggedInActivity and destroying the current activity
                            Intent intent = new Intent(LogInActivity.this, LoggedInActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        //Else showing the error message to the user
                        else
                        {
                            Toast.makeText(LogInActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            //If password is not filled prompt user using toast to fill the password
            else
            {
                Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            }
        }
        //If username is not filled prompt user using toast to fill the password
        else
        {
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
        }
    }
}
