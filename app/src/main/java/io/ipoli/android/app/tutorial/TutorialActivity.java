package io.ipoli.android.app.tutorial;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.tutorial.events.TutorialSkippedEvent;
import io.ipoli.android.app.tutorial.fragments.TutorialFragment;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.challenge.activities.PickChallengeActivity;

public class TutorialActivity extends AppIntro2 {

    public static final String SHOW_PICK_CHALLENGES = "show_pick_challenges";
    @Inject
    Bus eventBus;

    private boolean showPickChallenges = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(this).inject(this);

        if (IntentUtils.hasExtra(getIntent(), SHOW_PICK_CHALLENGES)) {
            showPickChallenges = getIntent().getBooleanExtra(SHOW_PICK_CHALLENGES, false);
        }

        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_welcome_title),
                getString(R.string.tutorial_hero_desc),
                R.drawable.tutorial_welcome,
                R.color.md_blue_700));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_challenges_title),
                getString(R.string.tutorial_challenges_desc),
                R.drawable.tutorial_challenge,
                R.color.md_light_blue_500));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_pet_title),
                getString(R.string.tutorial_pet_desc),
                R.drawable.tutorial_pet,
                R.color.md_orange_700));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_schedule_title),
                getString(R.string.tutorial_schedule_desc),
                R.drawable.tutorial_schedule,
                R.color.md_green_500));

        setImmersiveMode(true, true);
        setColorTransitionsEnabled(true);
        showSkipButton(false);
    }

    @Override
    public void onDonePressed(Fragment fragment) {
        doneButton.setVisibility(View.GONE);
        finish();
    }

    @Override
    public void onBackPressed() {
        eventBus.post(new TutorialSkippedEvent());
        super.onBackPressed();
    }

    @Override
    public void finish() {
        if (showPickChallenges) {
            Intent intent = new Intent(this, PickChallengeActivity.class);
            intent.putExtra(PickChallengeActivity.TITLE, getString(R.string.pick_challenge_to_start));
            startActivity(intent);
        }
        super.finish();
    }
}
