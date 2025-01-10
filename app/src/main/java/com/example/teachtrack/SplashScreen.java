package com.example.teachtrack;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {
    private ProgressBar pb;
    private TextView percentTextView;
    private int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        pb = findViewById(R.id.pb);
        percentTextView = findViewById(R.id.percentTextView);

        // Start the progress animation
        startProgressAnimation();

        // Navigate to the GoogleSignInActivity after a short delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
            }
        }, 2000); // Change the delay as needed
    }

    private void startProgressAnimation() {
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(pb, "progress", 0, 100);
        progressAnimator.setDuration(3000); // Set the duration of the animation
        progressAnimator.setInterpolator(new LinearInterpolator()); // Use a linear interpolator
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                counter = animatedValue;
                updatePercentageText();
            }
        });
        progressAnimator.start();
    }

    private void updatePercentageText() {
        String percentageText = counter + "%";
        percentTextView.setText(percentageText);
    }
}
