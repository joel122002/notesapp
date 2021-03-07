package com.notesapp.notes;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    //onCreate
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //This activity is responsible for the splash screen and therefore it has to later transition to the LogInActivity
        Intent intent = new Intent(SplashScreen.this, LogInActivity.class);
        startActivity(intent);
        //Here this activity is destroyed
        finish();
    }
}
