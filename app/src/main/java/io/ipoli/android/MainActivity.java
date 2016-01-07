package io.ipoli.android;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.experience_bar)
    ProgressBar experienceBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(experienceBar, "progress", experienceBar.getProgress(), experienceBar.getMax());
        progressAnimator.setDuration(android.R.integer.config_shortAnimTime);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();
    }
}