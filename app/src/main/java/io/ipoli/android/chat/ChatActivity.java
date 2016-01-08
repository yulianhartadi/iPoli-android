package io.ipoli.android.chat;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.BaseActivity;
import io.ipoli.android.R;
import io.ipoli.android.assistant.events.RenameAssistantEvent;

public class ChatActivity extends BaseActivity {

    @Bind(R.id.experience_bar)
    ProgressBar experienceBar;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Inject
    Bus eventBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(experienceBar, "progress", experienceBar.getProgress(), experienceBar.getMax());
        progressAnimator.setDuration(android.R.integer.config_shortAnimTime);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.start();
    }

    @Subscribe
    public void onRenameAssistant(RenameAssistantEvent e) {
        toolbar.setTitle(e.name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }
}