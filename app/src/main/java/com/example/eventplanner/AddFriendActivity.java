package com.example.eventplanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.TransitionInflater;
import android.view.Window;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;

public class AddFriendActivity extends AppCompatActivity {

    TextInputEditText etSearchQuery;
    Button btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        //getWindow().setExitTransition(new Explode());
        setupWindowAnimations();

        etSearchQuery = findViewById(R.id.etSearchQuery);
        btnSearch = findViewById(R.id.btnSearch);

    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }
}