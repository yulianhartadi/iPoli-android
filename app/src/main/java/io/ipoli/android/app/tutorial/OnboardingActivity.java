package io.ipoli.android.app.tutorial;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import io.ipoli.android.R;
import io.ipoli.android.app.tutorial.fragments.TutorialAddQuestFragment;
import io.ipoli.android.app.tutorial.fragments.TutorialCalendarFragment;
import io.ipoli.android.app.tutorial.fragments.TutorialIntroFragment;
import io.ipoli.android.app.tutorial.fragments.TutorialNamePromptFragment;
import io.ipoli.android.app.tutorial.fragments.TutorialOutroFragment;
import io.ipoli.android.quest.data.Category;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/31/17.
 */
public class OnboardingActivity extends AppCompatActivity {

    private String playerName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.root_container, new TutorialIntroFragment());
//        fragmentTransaction.replace(R.id.root_container, new TutorialAddQuestFragment());
//        fragmentTransaction.replace(R.id.root_container, new TutorialOutroFragment());
        fragmentTransaction.commit();
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//        }
//    }

    public void onIntroDone() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.root_container, new TutorialNamePromptFragment(), "fragment");
        transaction.commit();
    }

    public void onNamePromptDone(String playerName) {
        this.playerName = playerName;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.replace(R.id.root_container, new TutorialAddQuestFragment(), "fragment");
        transaction.commit();
    }

    public void onAddQuestDone(String name, Category category) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        TutorialCalendarFragment calendarFragment = new TutorialCalendarFragment();
        calendarFragment.setQuestInfo(name, category);
        transaction.replace(R.id.root_container, calendarFragment, "fragment");
        transaction.commit();
    }

    public void onCalendarDone() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        TutorialOutroFragment outroFragment = new TutorialOutroFragment();
        outroFragment.setPlayerName(playerName);
        transaction.replace(R.id.root_container, outroFragment, "fragment");
        transaction.commit();
    }

    public void onTutorialDone() {
        finish();
    }
}