package io.ipoli.android.tutorial;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.squareup.otto.Bus;

import java.util.ArrayList;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
import io.ipoli.android.tutorial.events.TutorialSkippedEvent;

public class TutorialActivity extends AppIntro2 {
    @Inject
    Bus eventBus;

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        addSlide(TutorialFragment.newInstance("Welcome to iPoli", "I'm your smart assistant. Your daily tasks are now called quests. Be a hero and complete them!", R.drawable.welcome_tutorial, false));
        addSlide(TutorialFragment.newInstance("Easily Schedule Your Day", "Simply touch, hold and drag a quest to schedule or reschedule it", R.drawable.calendar_tutorial));
        addSlide(TutorialFragment.newInstance("Review Your Weekly Schedule", "Swipe a quest to the right to complete it or to the left to schedule it for today", R.drawable.overview_tutorial));
        addSlide(TutorialFragment.newInstance("Quickly Add with Smart Assist", "Use everyday language and autocomplete to create your quests in a flash", R.drawable.add_quest_tutorial));
        addSlide(TutorialFragment.newInstance("Collect Unscheduled Quests in Inbox", "Swipe a quest to the right to schedule it for today or to the left to delete it", R.drawable.inbox_tutorial));
        addSlide(TutorialFragment.newInstance("Stick to Your Important Habits", "Track your weekly progress. Swipe a habit to the left to delete it.", R.drawable.habits_tutorial));

        int[] colors = new int[]{R.color.md_indigo_500, R.color.md_blue_500, R.color.md_green_500, R.color.md_orange_500,
                R.color.md_deep_purple_500, R.color.md_teal_500};
        ArrayList<Integer> c = new ArrayList<>();
        for(int color : colors) {
            c.add(ContextCompat.getColor(this, color));
        }

        setAnimationColors(c);
    }

    @Override
    public void onDonePressed() {
        eventBus.post(new TutorialDoneEvent());
        finish();
    }

    @Override
    public void onNextPressed() {

    }

    @Override
    public void onSlideChanged() {

    }

    @Override
    public void onBackPressed() {
        eventBus.post(new TutorialSkippedEvent());
        super.onBackPressed();
    }
}
