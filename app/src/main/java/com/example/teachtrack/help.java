package com.example.teachtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class help extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab12);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail(view);
            }
        });

        // Enable the back button on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the click listener for the back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    // Method to send an email

    public void sendEmail(View view) {
        // Create an intent to send an email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"teachtrackapp@gmail.com"}); // Replace with the recipient's email address
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body of the email.");

        // Start the email intent
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }
}

