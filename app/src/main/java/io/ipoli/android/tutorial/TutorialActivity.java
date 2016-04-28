package io.ipoli.android.tutorial;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;

import com.github.paolorotolo.appintro.AppIntro2;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.ForceSyncRequestEvent;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import io.ipoli.android.tutorial.events.TutorialDoneEvent;
import io.ipoli.android.tutorial.events.TutorialSkippedEvent;
import io.ipoli.android.tutorial.fragments.PickHabitsFragment;
import io.ipoli.android.tutorial.fragments.PickQuestsFragment;
import io.ipoli.android.tutorial.fragments.TutorialFragment;

public class TutorialActivity extends AppIntro2 {
    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    private PickHabitsFragment pickHabitsFragment;
    private PickQuestsFragment pickQuestsFragment;

    @Override
    public void init(@Nullable Bundle savedInstanceState) {
        App.getAppComponent(this).inject(this);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_welcome_title), getString(R.string.tutorial_welcome_desc), R.drawable.tutorial_welcome, false));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_calendar_title), getString(R.string.tutorial_calendar_desc), R.drawable.tutorial_calendar));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_add_quest_title), getString(R.string.tutorial_add_quest_desc), R.drawable.tutorial_add_quest));
        addSlide(TutorialFragment.newInstance(getString(R.string.tutorial_inbox_title), getString(R.string.tutorial_inbox_desc), R.drawable.tutorial_inbox));
        pickQuestsFragment = new PickQuestsFragment();
        addSlide(pickQuestsFragment);
        pickHabitsFragment = new PickHabitsFragment();
        addSlide(pickHabitsFragment);

        int[] colors = new int[]{
                R.color.md_indigo_500,
                R.color.md_blue_500,
                R.color.md_orange_500,
                R.color.md_deep_purple_500,
                R.color.md_blue_500,
                R.color.md_blue_500
        };
        ArrayList<Integer> c = new ArrayList<>();
        for (int color : colors) {
            c.add(ContextCompat.getColor(this, color));
        }

        setAnimationColors(c);
    }

    @Override
    public void onDonePressed() {
        List<Quest> selectedQuests = pickQuestsFragment.getSelectedQuests();
        questPersistenceService.saveRemoteObjects(selectedQuests);
        List<RecurrentQuest> selectedHabits = pickHabitsFragment.getSelectedQuests();
        recurrentQuestPersistenceService.saveRemoteObjects(selectedHabits);
        eventBus.post(new ForceSyncRequestEvent());
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
