package io.ipoli.android.app.tutorial.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.tutorial.PickQuestViewModel;
import io.ipoli.android.app.tutorial.adapters.PickTutorialQuestsAdapter;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickTutorialQuestsFragment extends BaseTutorialPickQuestsFragment<Quest> implements ISlideBackgroundColorHolder {

    @Inject
    Bus eventBus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getContext()).inject(this);
    }

    @Override
    protected int getTitleRes() {
        return R.string.title_pick_initial_quests;
    }

    @Override
    protected void initAdapter() {
        pickQuestsAdapter = new PickTutorialQuestsAdapter(getContext(), eventBus, viewModels);
    }

    @Override
    protected void initViewModels() {
        viewModels = new ArrayList<>();

        Quest q = makeQuest("Try out iPoli", Category.FUN);
        q.setStartTime(Time.afterMinutes(5));
        q.setDuration(Constants.QUEST_MIN_DURATION);
        viewModels.add(new PickQuestViewModel(q, q.getName(), true));

        addViewModel("Take a walk", Category.WELLNESS, "Take a walk for 25 min", 25);
        addViewModel("Answer emails", Category.WORK);
        addViewModel("Make a lunch date", Category.FUN);
        addViewModel("Call dad", Category.PERSONAL);
        addViewModel("Cook a nice dinner", Category.FUN);
        addViewModel("Hit the gym", Category.WELLNESS);
        addViewModel("Plan a date night", Category.FUN);
        addViewModel("Call mom", Category.PERSONAL);
        addViewModel("Do laundry", Category.CHORES);
        addViewModel("Rent a bouncy castle", Category.FUN);
        addViewModel("Call plumber", Category.CHORES);
        addViewModel("Take my vitamin", Category.WELLNESS);
        addViewModel("Find dog sitter", Category.CHORES);
        addViewModel("Prep for presentation", Category.WORK);
        addViewModel("Check my weight", Category.WELLNESS);
        addViewModel("Play my favourite song and dance", Category.FUN);
    }

    private void addViewModel(String name, Category category) {
        addViewModel(name, category, false);
    }

    private void addViewModel(String name, Category category, boolean isSelected) {
        Quest q = makeQuest(name, category);
        q.setDuration(Constants.QUEST_MIN_DURATION);
        viewModels.add(new PickQuestViewModel(q, name, isSelected));
    }

    private void addViewModel(String name, Category category, String text, int duration) {
        Quest q = makeQuest(name, category);
        q.setDuration(duration);
        viewModels.add(new PickQuestViewModel(q, text));
    }

    @NonNull
    private Quest makeQuest(String name, Category category) {
        Quest q = new Quest(name, LocalDate.now());
        q.setCategory(category.name());
        q.setRawText(name + " today");
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder(0, String.valueOf(new Random().nextInt())));
        q.setReminders(reminders);
        return q;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return ContextCompat.getColor(getContext(), R.color.md_white);
    }
}
