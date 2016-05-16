package io.ipoli.android.quest.fragments;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.adapters.OverviewAdapter;
import io.ipoli.android.quest.data.Habit;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.viewmodels.QuestViewModel;

public class OverviewFragment extends Fragment {
    @Inject
    Bus eventBus;

    @BindView(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private OverviewAdapter overviewAdapter;
    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        unbinder = ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        overviewAdapter = new OverviewAdapter(getContext(), new ArrayList<>(), eventBus);
        questList.setAdapter(overviewAdapter);

        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        ItemTouchCallback touchCallback = new ItemTouchCallback(overviewAdapter, 0, swipeFlags);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeEndDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_green_500)));
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_blue_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
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

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        Date endDate = new Date();
        String toast = getString(R.string.quest_scheduled_for_today);
        if (e.quest.isScheduledForToday()) {
            toast = getString(R.string.quest_scheduled_for_tomorrow);
            endDate = DateUtils.getTomorrow();
        }
        q.setEndDate(endDate);
        questPersistenceService.save(q);
        Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
        updateQuests();
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateQuests();
    }

    private void updateQuests() {
        questPersistenceService.findPlannedBetween(new LocalDate(), new LocalDate().plusDays(7)).subscribe(quests -> {

            List<QuestViewModel> viewModels = new ArrayList<>();
            List<Quest> recurrent = new ArrayList<>();
            for (Quest q : quests) {
                if (q.isScheduledForToday() && q.getHabit() != null && !TextUtils.isEmpty(q.getHabit().getRecurrence().getDailyRrule())) {
                    recurrent.add(q);
                } else {
                    viewModels.add(new QuestViewModel(getContext(), q, 1, 1));
                }
            }

            Map<String, List<Quest>> map = new HashMap<>();
            for (Quest q : recurrent) {
                String key = q.getHabit().getId();
                if (map.get(key) == null) {
                    map.put(key, new ArrayList<>());
                }
                map.get(key).add(q);
            }

            for (String key : map.keySet()) {
                Quest q = map.get(key).get(0);
                Habit rq = q.getHabit();
                try {
                    Recur recur = new Recur(rq.getRecurrence().getDailyRrule());
                    int repeatCount = recur.getCount();
                    int remainingCount = map.get(key).size();
                    viewModels.add(new QuestViewModel(getContext(), q, repeatCount, remainingCount));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Collections.sort(viewModels, new Comparator<QuestViewModel>() {
                @Override
                public int compare(QuestViewModel lhs, QuestViewModel rhs) {
                    Quest lq = lhs.getQuest();
                    Quest rq = rhs.getQuest();
                    if (lq.getEndDate().before(rq.getEndDate())) {
                        return -1;
                    }
                    if (lq.getEndDate().after(rq.getEndDate())) {
                        return 1;
                    }
                    return lhs.getQuest().getStartMinute() > rhs.getQuest().getStartMinute() ? 1 : -1;
                }
            });
            overviewAdapter.updateQuests(viewModels);
        });
    }
}
