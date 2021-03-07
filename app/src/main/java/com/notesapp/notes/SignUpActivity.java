package com.notesapp.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{
    //Creating instance variables
    //TextInputEditTexts that hold the username password and the confimation password
    private TextInputEditText textInputEditTextUsername, textInputEditTextPassword, textInputEditTextPasswordConfirm;
    //This is the button the user clicks to sign up
    private Button buttonSignUp;
    //This is the hyperlink text that is responsible to transition to the LogInActivity
    private TextView textViewLogin;

    //onCreate() method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //Initializing the instance variables
        textInputEditTextUsername = findViewById(R.id.textInputEditTextUsername);
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        textInputEditTextPasswordConfirm = findViewById(R.id.textInputEditTextPasswordConfirm);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewLogin = findViewById(R.id.textViewLogin);
        //Setting up the OnClickListeners for the button and the hyperlink text
        buttonSignUp.setOnClickListener(this);
        textViewLogin.setOnClickListener(this);
    }

    //onClick() method
    @Override
    public void onClick(View v) {
        //Switching between blocks of code based on the Item that is clicked
        switch(v.getId())
        {
            //Code for signing up
            case R.id.buttonSignUp :
                //method that will sign up the user
                signUp();
                break;
            //If the hyperlink is clicked transition to LogInActivity
            case R.id.textViewLogin :
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(intent);
                //Destroying the current activity
                finish();
                break;

        }

    }

    //signUp() method will sign up the user
    private void signUp()
    {
        //If username is not empty
        if(!textInputEditTextUsername.getText().toString().isEmpty())
        {
            //If password is not empty
            if (!textInputEditTextPassword.getText().toString().isEmpty())
            {
                //If confirm password is not empty
                if (!textInputEditTextPasswordConfirm.getText().toString().isEmpty())
                {
                    //If password is equal to confirm password
                    if(textInputEditTextPassword.getText().toString().matches(textInputEditTextPasswordConfirm.getText().toString()))
                    {
                        ParseUser newUser = new ParseUser();
                        newUser.setUsername(textInputEditTextUsername.getText().toString());
                        newUser.setPassword(textInputEditTextPassword.getText().toString());
                        newUser.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null)
                                {
                                    Toast.makeText(SignUpActivity.this, "SignUp Successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(SignUpActivity.this,LoggedInActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else
                                {
                                    Toast.makeText(SignUpActivity.this, "Login Unsucessful", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                    //Else password is not equal to confirm password
                    else
                    {
                        //Prompt user to fill in the same passwords in password and password confirm
                        Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    }
                }
                //Else confirm password is empty
                else
                {
                    //Prompt user to fill in the confirmation password
                    Toast.makeText(SignUpActivity.this, "Please confirm your Password", Toast.LENGTH_SHORT).show();
                }
            }
            //Else password is empty
            else
            {
                //Prompt user to fill in the password
                Toast.makeText(SignUpActivity.this, "Please enter a Password", Toast.LENGTH_SHORT).show();
            }
        }
        //Else username is empty
        else
        {
            //Prompt user to fill in the username
            Toast.makeText(SignUpActivity.this, "Please enter an Username", Toast.LENGTH_SHORT).show();
        }
    }
}
