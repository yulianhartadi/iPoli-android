package io.ipoli.android.tutorial.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.squareup.otto.Bus;

import java.util.ArrayList;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.QuestContext;
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

        addViewModel("Set today's goals every Mon Tue Wed Thur and Fri at 9:00", QuestContext.PERSONAL, true);
        addViewModel("Exercise every Mon Wed and Fri", QuestContext.WELLNESS);
        addViewModel("Walk the dog every day", QuestContext.FUN);
        addViewModel("Be mindful for 15 min every day", QuestContext.WELLNESS);
        addViewModel("Review my day every Mon Tue Wed Thur and Fri at 22:00", QuestContext.PERSONAL, true);
        addViewModel("Learn new language every day", QuestContext.LEARNING);
        addViewModel("Drink water 6 times per day every day", QuestContext.WELLNESS);
        addViewModel("Floss every day", QuestContext.WELLNESS);
        addViewModel("Read every day for 1 hour", QuestContext.LEARNING);
        addViewModel("Go for a run every Tue and Thur", QuestContext.WELLNESS);
        addViewModel("Stretch every Mon Tue Wed Thur and Fri", QuestContext.WELLNESS);
        addViewModel("Pay bills every 20th of the month", QuestContext.CHORES);
        addViewModel("Answer emails every Mon Tue Wed Thur and Fri", QuestContext.WORK);
        addViewModel("Call mom and dad every Sun", QuestContext.PERSONAL);
        addViewModel("Do laundry every Sun", QuestContext.CHORES);

    }

    private void addViewModel(String text, QuestContext context) {
        addViewModel(text, context, false);
    }

    private void addViewModel(String text, QuestContext context, boolean isSelected) {
        RepeatingQuest rq = new RepeatingQuest(text);
        RepeatingQuest.setContext(rq, context);
        viewModels.add(new PickQuestViewModel<>(rq, text, isSelected));
    }

    @Override
    public int getDefaultBackgroundColor() {
        return ContextCompat.getColor(getContext(), R.color.md_white);
    }
}
