package io.ipoli.android.quest.fragments;


import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.DateTime;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.services.events.SyncCompleteEvent;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.adapters.HabitsAdapter;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.events.DeleteRecurrentQuestEvent;
import io.ipoli.android.quest.events.DeleteRecurrentQuestRequestEvent;
import io.ipoli.android.quest.events.UndoDeleteRecurrentQuestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import io.ipoli.android.quest.viewmodels.RecurrentQuestViewModel;

public class HabitsFragment extends Fragment {

    @Inject
    Bus eventBus;

    CoordinatorLayout rootContainer;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    @Inject
    QuestPersistenceService questPersistenceService;

    private HabitsAdapter habitsAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_habits, container, false);
        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(new SimpleDateFormat(getString(R.string.today_date_format), Locale.getDefault()).format(new java.util.Date()));

        rootContainer = (CoordinatorLayout) getActivity().findViewById(R.id.root_container);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        habitsAdapter = new HabitsAdapter(getContext(), new ArrayList<>(), eventBus);
        questList.setAdapter(habitsAdapter);

        int swipeFlags = ItemTouchHelper.START;
        ItemTouchCallback touchCallback = new ItemTouchCallback(habitsAdapter, 0, swipeFlags);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_red_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
            for (RecurrentQuest rq : quests) {
                RecurrentQuestViewModel vm = createViewModel(rq);
                if (vm != null) {
                    viewModels.add(vm);
                }
            }
            habitsAdapter.updateQuests(viewModels);
        });
    }

    @Nullable
    private RecurrentQuestViewModel createViewModel(RecurrentQuest rq) {
        try {
            Recurrence recurrence = rq.getRecurrence();
            Recur recur = new Recur(recurrence.getRrule());

            LocalDate from, to;
            if (recur.getFrequency().equals(Recur.MONTHLY)) {
                from = LocalDate.now().dayOfMonth().withMinimumValue();
                to = LocalDate.now().dayOfMonth().withMaximumValue();
            } else {
                from = LocalDate.now().dayOfWeek().withMinimumValue();
                to = LocalDate.now().dayOfWeek().withMaximumValue();
            }

            int completedCount = (int) questPersistenceService.countCompletedQuests(rq, from, to);

            Date todayStartOfDay = LocalDate.now().toDateTimeAtStartOfDay(DateTimeZone.UTC).toDate();

            java.util.Date nextDate = recur.getNextDate(new DateTime(recurrence.getDtstart()), new DateTime(todayStartOfDay));

            if (DateUtils.isTodayUTC(nextDate)) {
                int completedForToday = (int) questPersistenceService.countCompletedQuests(rq, LocalDate.now(), LocalDate.now());
                int timesPerDay = TextUtils.isEmpty(recurrence.getDailyRrule()) ? 1 : new Recur(recurrence.getDailyRrule()).getCount();
                if (completedForToday >= timesPerDay) {
                    Date tomorrowStartOfDay = LocalDate.now().toDateTimeAtStartOfDay(DateTimeZone.UTC).plusDays(1).toDate();
                    nextDate = recur.getNextDate(new DateTime(recurrence.getDtstart()), new DateTime(tomorrowStartOfDay));
                    if (recurrence.getDtend() != null && nextDate.after(recurrence.getDtend())) {
                        nextDate = null;
                    }
                }
            }
            return new RecurrentQuestViewModel(rq, completedCount, recur, nextDate);
        } catch (ParseException e) {
            return null;
        }
    }

    @Subscribe
    public void onRecurrentQuestDeleteRequest(final DeleteRecurrentQuestRequestEvent e) {
        final Snackbar snackbar = Snackbar
                .make(rootContainer,
                        R.string.habit_removed,
                        Snackbar.LENGTH_LONG);

        final RecurrentQuest rq = e.recurrentQuest;

        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
//                questPersistenceService.delete(quest);
                eventBus.post(new DeleteRecurrentQuestEvent(rq));
            }
        });

        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecurrentQuestViewModel vm = createViewModel(rq);
                if (vm != null) {
                    habitsAdapter.addQuest(e.position, vm);
                }
                snackbar.setCallback(null);
                eventBus.post(new UndoDeleteRecurrentQuestEvent(rq));
            }
        });

        snackbar.show();
    }

    @Subscribe
    public void onSyncComplete(SyncCompleteEvent e) {
        updateQuests();
    }
}
