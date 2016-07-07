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

        addViewModel("Exercise 3 times a week", Category.WELLNESS, true);
        addViewModel("Walk the dog every day", Category.FUN);
        addViewModel("Be mindful for 15 min 3 times a week", Category.WELLNESS, true);
        addViewModel("Review my day every Mon Tue Wed Thur and Fri at 22:00", Category.PERSONAL, true);
        addViewModel("Learn new language every day", Category.LEARNING);
        addViewModel("Drink water 6 times a day every day", Category.WELLNESS);
        addViewModel("Floss every day", Category.WELLNESS, true);
        addViewModel("Read for 30 min 4 times a week ", Category.LEARNING);
        addViewModel("Go for a run 2 times a week", Category.WELLNESS);
        addViewModel("Pay bills 1 time a month", Category.CHORES);
        addViewModel("Answer emails every Mon Tue Wed Thur and Fri", Category.WORK);
        addViewModel("Meditate 3 times a week for 10 min", Category.WELLNESS);
        addViewModel("Call mom and dad 2 times a month", Category.PERSONAL);
        addViewModel("Stretch 5 times a week", Category.WELLNESS);
        addViewModel("Do laundry every Sun", Category.CHORES);
        addViewModel("Take a pill every day", Category.WELLNESS);
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
