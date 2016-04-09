package io.ipoli.android.quest.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;

import com.roughike.bottombar.BottomBar;
import com.squareup.otto.Bus;

import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Date;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.util.Dates;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.BottomBarUtil;
import io.ipoli.android.R;
import io.ipoli.android.app.BaseActivity;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.adapters.HabitsAdapter;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import io.ipoli.android.quest.viewmodels.RecurrentQuestViewModel;

public class HabitsActivity extends BaseActivity {
    @Inject
    Bus eventBus;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    private HabitsAdapter habitsAdapter;
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits);
        bottomBar = BottomBarUtil.getBottomBar(this, R.id.root_container, R.id.quest_list, savedInstanceState, BottomBarUtil.HABITS_TAB_INDEX);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(new SimpleDateFormat(getString(R.string.today_date_format), Locale.getDefault()).format(new java.util.Date()));
        }

        appComponent().inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        habitsAdapter = new HabitsAdapter(this, new ArrayList<>(), eventBus);
        questList.setAdapter(habitsAdapter);

        int swipeFlags = ItemTouchHelper.START;
        ItemTouchCallback touchCallback = new ItemTouchCallback(habitsAdapter, 0, swipeFlags);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.md_red_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        bottomBar.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
        updateQuests();
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    private void updateQuests() {
        recurrentQuestPersistenceService.findAllHabits().subscribe(quests -> {
            List<RecurrentQuestViewModel> viewModels = new ArrayList<>();
            for (RecurrentQuest q : quests) {
                try {
                    Recurrence recurrence = q.getRecurrence();
                    Recur recur = new Recur(recurrence.getRrule());

                    java.util.Date from, to;
                    if (recur.getFrequency().equals(Recur.MONTHLY)) {
                        from = DateUtils.getFirstDateOfMonth();
                        to = DateUtils.getLastDateOfMonth();
                    } else {
                        from = DateUtils.getFirstDateOfWeek();
                        to = DateUtils.getLastDateOfWeek();
                    }

                    int completedCount = (int) questPersistenceService.countCompletedQuests(q, from, to);

                    java.util.Date nextDate = Dates.getCalendarInstance(recur.getNextDate(new Date(recurrence.getDtstart()), new Date())).getTime();

                    if (DateUtils.isToday(nextDate)) {
                        int completedForToday = (int) questPersistenceService.countCompletedQuests(q, DateUtils.getTodayAtMidnight().getTime(), DateUtils.getTodayAtMidnight().getTime());
                        int timesPerDay = TextUtils.isEmpty(recurrence.getDailyRrule()) ? 1 : new Recur(recurrence.getDailyRrule()).getInterval();
                        if (completedForToday >= timesPerDay) {
                            nextDate = recur.getNextDate(new Date(recurrence.getDtstart()), new Date(DateUtils.getTomorrow()));
                            if (recurrence.getDtend() != null && nextDate.after(recurrence.getDtend())) {
                                nextDate = null;
                            }
                        }
                    }
                    viewModels.add(new RecurrentQuestViewModel(q, completedCount, recur, nextDate));
                } catch (ParseException e) {
                }
            }
            habitsAdapter.updateQuests(viewModels);
        });

    }
}
