package com.example.teachtrack;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AboutUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        // Find the teamSection view
        final LinearLayout teamSection = findViewById(R.id.teamSection);
        Toolbar toolbar = findViewById(R.id.aboutapp);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set the click listener for the back button
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        // Create a fade-in animation
        Animation fadeInAnimation = new AlphaAnimation(0, 1);
        fadeInAnimation.setDuration(1500); // Set the animation duration in milliseconds

        // Set an animation listener to make the view visible when the animation starts
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                teamSection.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        // Start the animation
        teamSection.startAnimation(fadeInAnimation);
    }
    public void sendEmail(View view) {
        // Create an intent to send an email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "rhythmdebnath2002@gmail.com" }); // Replace with the recipient's email address
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body of the email.");

        // Start the email intent
        startActivity(Intent.createChooser(emailIntent, "Send Email"));
    }
    public void sendEmail2(View view) {
        // Create an intent to send an email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { "ag.khushigupta@gmail.com" }); // Replace with the recipient's email address
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Body of the email.");

        // Start the email intent
        startActivity(Intent.createChooser(emailIntent, "Send Email2"));
    }
}


