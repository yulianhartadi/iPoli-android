package io.ipoli.android.tutorial.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.squareup.otto.Bus;

import java.util.ArrayList;

import javax.inject.Inject;

import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.tutorial.PickQuestViewModel;
import io.ipoli.android.tutorial.adapters.PickQuestsAdapter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/27/16.
 */
public class PickQuestsFragment extends BasePickQuestsFragment<Quest> implements ISlideBackgroundColorHolder {
    @Inject
    Bus eventBus;
    private int backgroundColor;

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
        pickQuestsAdapter = new PickQuestsAdapter(getContext(), eventBus, viewModels);
    }

    @Override
    protected void initViewModels() {
        viewModels = new ArrayList<>();

        Quest q = makeQuest("Try out iPoli", QuestContext.FUN);
        Quest.setStartTime(q, Time.afterMinutes(5));
        viewModels.add(new PickQuestViewModel<>(q, q.getName(), true));

        addViewModel("Take a walk", QuestContext.WELLNESS, "Take a walk for 25 min", 25);
        addViewModel("Answer emails", QuestContext.WORK);
        addViewModel("Make a lunch date", QuestContext.FUN);
        addViewModel("Call dad", QuestContext.PERSONAL);
        addViewModel("Cook a nice dinner", QuestContext.FUN);
        addViewModel("Hit the gym", QuestContext.WELLNESS);
        addViewModel("Plan a date night", QuestContext.FUN);
        addViewModel("Call mom", QuestContext.PERSONAL);
        addViewModel("Do laundry", QuestContext.CHORES);
        addViewModel("Rent a bouncy castle", QuestContext.FUN);
        addViewModel("Call plumber", QuestContext.CHORES);
        addViewModel("Take my vitamin", QuestContext.WELLNESS);
        addViewModel("Find dog sitter", QuestContext.CHORES);
        addViewModel("Prep for presentation", QuestContext.WORK);
        addViewModel("Check my weight", QuestContext.WELLNESS);
        addViewModel("Play my favourite song and dance", QuestContext.FUN);
    }

    private void addViewModel(String name, QuestContext context) {
        addViewModel(name, context, false);
    }

    private void addViewModel(String name, QuestContext context, boolean isSelected) {
        Quest q = makeQuest(name, context);
        viewModels.add(new PickQuestViewModel<>(q, name, isSelected));
    }

    private void addViewModel(String name, QuestContext context, String text, int duration) {
        Quest q = makeQuest(name, context);
        q.setDuration(duration);
        viewModels.add(new PickQuestViewModel<>(q, text));
    }

    @NonNull
    private Quest makeQuest(String name, QuestContext context) {
        Quest q = new Quest(name, DateUtils.now());
        Quest.setContext(q, context);
        q.setRawText(name + " today");
        return q;
    }

    @Override
    public int getDefaultBackgroundColor() {
        return ContextCompat.getColor(getContext(), R.color.md_blue_500);
    }
}
