package io.ipoli.android.quest.fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.mobiwise.materialintro.shape.Focus;
import io.ipoli.android.R;
import io.ipoli.android.tutorial.Tutorial;
import io.ipoli.android.tutorial.TutorialItem;
import io.ipoli.android.app.App;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.OverviewAdapter;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.events.ScheduleQuestForTodayEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class OverviewFragment extends Fragment {

    @Inject
    Bus eventBus;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Inject
    QuestPersistenceService questPersistenceService;

    private OverviewAdapter overviewAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_overview, container, false);
        ButterKnife.bind(this, v);
        App.getAppComponent(getContext()).inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        overviewAdapter = new OverviewAdapter(getContext(), new ArrayList<Quest>(), eventBus);
        questList.setAdapter(overviewAdapter);

        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        ItemTouchCallback touchCallback = new ItemTouchCallback(overviewAdapter, 0, swipeFlags);
        touchCallback.setLongPressDragEnabled(false);
        touchCallback.setSwipeEndDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_green_500)));
        touchCallback.setSwipeStartDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), R.color.md_blue_500)));
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);

        return v;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isResumed() && isVisibleToUser) {
            updateQuests();
            Tutorial.getInstance(getContext()).addItem(
                    new TutorialItem.Builder(getActivity())
                            .setState(Tutorial.State.TUTORIAL_OVERVIEW_SWIPE)
                            .setTarget(questList)
                            .setFocusType(Focus.MINIMUM)
                            .enableDotAnimation(false)
                            .dismissOnTouch(true)
                            .build());
        }
    }

    @Subscribe
    public void onScheduleQuestForToday(ScheduleQuestForTodayEvent e) {
        Quest q = e.quest;
        Date due = new Date();
        String toast = getString(R.string.quest_scheduled_for_today);
        if (DateUtils.isToday(e.quest.getDue())) {
            toast = getString(R.string.quest_scheduled_for_tomorrow);
            due = DateUtils.getTomorrow();
        }
        q.setDue(due);
        questPersistenceService.save(q);
        updateQuests();
        Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onQuestSnoozed(QuestSnoozedEvent e) {
        updateQuests();

    }

    private void updateQuests() {
        overviewAdapter.updateQuests(questPersistenceService.findAllPlanned());
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
}
