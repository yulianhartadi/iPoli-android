package io.ipoli.android.challenge.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.reminders.data.Reminder;
import io.ipoli.android.tutorial.PickQuestViewModel;
import io.ipoli.android.tutorial.adapters.PickTutorialQuestsAdapter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/13/16.
 */
public class PersonalizeChallengeActivity extends BaseActivity {

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.appbar)
    AppBarLayout appBar;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;

    @BindView(R.id.predefined_challenge_quests)
    RecyclerView questList;
    private ArrayList<PickQuestViewModel> viewModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_personalize_challenge);
        ButterKnife.bind(this);
        appComponent().inject(this);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setDisplayShowTitleEnabled(false);
        }

        collapsingToolbar.setTitle("Master Presenter");

        questList.setLayoutManager(new LinearLayoutManager(this));
        questList.setHasFixedSize(true);
        initViewModels();
        questList.setAdapter(new PickTutorialQuestsAdapter(this, eventBus, viewModels));
    }

    protected void initViewModels() {
        viewModels = new ArrayList<>();

        Quest q = makeQuest("Try out iPoli", Category.FUN);
        Quest.setStartTime(q, Time.afterMinutes(5));
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
        Quest q = new Quest(name, DateUtils.now());
        q.setCategory(category.name());
        q.setRawText(name + " today");
        List<Reminder> reminders = new ArrayList<>();
        reminders.add(new Reminder(0, new Random().nextInt()));
        q.setReminders(reminders);
        return q;
    }
}
