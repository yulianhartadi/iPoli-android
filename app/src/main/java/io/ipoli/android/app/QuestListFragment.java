package io.ipoli.android.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.ItemTouchCallback;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestAdapter;
import io.ipoli.android.quest.QuestContext;
import io.ipoli.android.quest.Status;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/17/16.
 */
public class QuestListFragment extends Fragment {

    @Inject
    Bus eventBus;

    @Bind(R.id.quest_list)
    RecyclerView questList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_quest_list, container, false);
        ButterKnife.bind(this, v);
        App.getAppComponent(getContext()).inject(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        questList.setLayoutManager(layoutManager);

        Quest q = new Quest("Go for a run", Status.PLANNED.name(), new Date());
        Quest.setContext(q, QuestContext.WELLNESS);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        q.setStartTime(calendar.getTime());
        q.setDuration(30);

        Quest qq = new Quest("Read a book", Status.PLANNED.name(), new Date());
        Quest.setContext(qq, QuestContext.LEARNING);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 30);
        qq.setStartTime(calendar.getTime());
        qq.setDuration(60);

        Quest qqq = new Quest("Call Mom", Status.PLANNED.name(), new Date());
        Quest.setContext(qqq, QuestContext.PERSONAL);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 15);
        qqq.setStartTime(calendar.getTime());
        qqq.setDuration(15);

        Quest qqqq = new Quest("Work on presentation", Status.PLANNED.name(), new Date());
        Quest.setContext(qqqq, QuestContext.WORK);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        qqqq.setStartTime(calendar.getTime());
        qqqq.setDuration(120);

        Quest qqqqq = new Quest("Watch Star Wars", Status.PLANNED.name(), new Date());
        Quest.setContext(qqqqq, QuestContext.FUN);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);
        qqqqq.setStartTime(calendar.getTime());
        qqqqq.setDuration(180);

        List<Quest> quests = new ArrayList<>();
        quests.add(q);
        quests.add(qq);
        quests.add(qqq);
        quests.add(qqqq);
        quests.add(qqqqq);

        Quest tq = new Quest("Work on presentation", Status.PLANNED.name(), new Date());
        Quest.setContext(tq, QuestContext.WORK);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        tq.setStartTime(calendar.getTime());
        tq.setDuration(120);

        Calendar c = Calendar.getInstance();
        c.setTime(tq.getDue());
        c.add(Calendar.DAY_OF_YEAR, 1);
        tq.setDue(c.getTime());

        quests.add(tq);

        Quest uq = new Quest("Jump on a single foot", Status.PLANNED.name(), new Date());
        Quest.setContext(uq, QuestContext.WELLNESS);
        calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 15);
        uq.setStartTime(calendar.getTime());
        uq.setDuration(15);

        c = Calendar.getInstance();
        c.setTime(uq.getDue());
        c.add(Calendar.DAY_OF_YEAR, 1);
        uq.setDue(c.getTime());
        quests.add(uq);

        QuestAdapter questAdapter = new QuestAdapter(getContext(), quests, eventBus);
        questList.setAdapter(questAdapter);

        int swipeFlags = ItemTouchHelper.END;
        ItemTouchCallback touchCallback = new ItemTouchCallback(questAdapter, swipeFlags, 0);
        touchCallback.setLongPressDragEnabled(false);
        ItemTouchHelper helper = new ItemTouchHelper(touchCallback);
        helper.attachToRecyclerView(questList);

        return v;
    }
}
