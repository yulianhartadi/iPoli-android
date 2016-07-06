package io.ipoli.android.tutorial.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.squareup.otto.Bus;

import java.util.ArrayList;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.Category;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.tutorial.PickQuestViewModel;
import io.ipoli.android.tutorial.adapters.PickRepeatingQuestsAdapter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickRepeatingQuestsFragment extends BasePickQuestsFragment<RepeatingQuest> {

    @Inject
    Bus eventBus;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(getContext()).inject(this);
    }

    @Override
    protected int getTitleRes() {
        return R.string.title_pick_initial_repeating_quests;
    }

    @Override
    protected void initAdapter() {
        pickQuestsAdapter = new PickRepeatingQuestsAdapter(getContext(), eventBus, viewModels);
    }

    @Override
    protected void initViewModels() {
        viewModels = new ArrayList<>();

        addViewModel("Set today's goals every Mon Tue Wed Thur and Fri at 9:00", Category.PERSONAL, true);
        addViewModel("Exercise 3 times a week", Category.WELLNESS);
        addViewModel("Walk the dog every day", Category.FUN);
        addViewModel("Be mindful for 15 min every day", Category.WELLNESS);
        addViewModel("Review my day every Mon Tue Wed Thur and Fri at 22:00", Category.PERSONAL, true);
        addViewModel("Learn new language every day", Category.LEARNING);
        addViewModel("Drink water 6 times a day every day", Category.WELLNESS);
        addViewModel("Floss every day", Category.WELLNESS);
        addViewModel("Read every day for 1 hour", Category.LEARNING);
        addViewModel("Go for a run 2 times a week", Category.WELLNESS);
        addViewModel("Stretch every Mon Tue Wed Thur and Fri", Category.WELLNESS);
        addViewModel("Pay bills every 20th of the month", Category.CHORES);
        addViewModel("Answer emails every Mon Tue Wed Thur and Fri", Category.WORK);
        addViewModel("Call mom and dad every Sun", Category.PERSONAL);
        addViewModel("Do laundry every Sun", Category.CHORES);
    }

    private void addViewModel(String text, Category category) {
        addViewModel(text, category, false);
    }

    private void addViewModel(String text, Category category, boolean isSelected) {
        RepeatingQuest rq = new RepeatingQuest(text);
        RepeatingQuest.setCategory(rq, category);
        viewModels.add(new PickQuestViewModel<>(rq, text, isSelected));
    }

    @Override
    public int getDefaultBackgroundColor() {
        return ContextCompat.getColor(getContext(), R.color.md_white);
    }
}
