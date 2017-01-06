package io.ipoli.android.quest.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class QuestRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private List<Quest> quests;

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    QuestPersistenceService questPersistenceService;

    public QuestRemoteViewsFactory(Context context) {
        App.getAppComponent(context).inject(this);
        this.quests = new ArrayList<>();
        this.context = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        final CountDownLatch latch = new CountDownLatch(1);
        questPersistenceService.findAllNonAllDayIncompleteForDate(LocalDate.now(), result -> {
            quests = result;
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        quests.clear();
    }

    @Override
    public int getCount() {
        return quests.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position >= getCount()) {
            return getLoadingView();
        }

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_agenda_item);
        Quest q = quests.get(position);
        rv.setTextViewText(R.id.widget_agenda_quest_name, getQuestName(q));
        rv.setImageViewResource(R.id.widget_agenda_category, q.getCategoryType().colorfulImage);

        Bundle tapQuestBundle = new Bundle();
        tapQuestBundle.putInt(AgendaWidgetProvider.QUEST_ACTION_EXTRA_KEY, AgendaWidgetProvider.QUEST_ACTION_VIEW);
        tapQuestBundle.putString(Constants.QUEST_ID_EXTRA_KEY, q.getId());
        Intent tapQuestIntent = new Intent();
        tapQuestIntent.putExtras(tapQuestBundle);
        rv.setOnClickFillInIntent(R.id.widget_agenda_quest_info_container, tapQuestIntent);

        Bundle completeQuestBundle = new Bundle();
        completeQuestBundle.putInt(AgendaWidgetProvider.QUEST_ACTION_EXTRA_KEY, AgendaWidgetProvider.QUEST_ACTION_COMPLETE);
        completeQuestBundle.putString(Constants.QUEST_ID_EXTRA_KEY, q.getId());

        Intent completeQuestIntent = new Intent();
        completeQuestIntent.putExtras(completeQuestBundle);
        rv.setOnClickFillInIntent(R.id.widget_agenda_check, completeQuestIntent);

        int duration = q.getDuration();
        Time startTime = Quest.getStartTime(q);
        String questTime = "";
        if (duration > 0 && startTime != null) {
            Time endTime = Time.plusMinutes(startTime, duration);
            questTime = startTime + " - " + endTime;
        } else if (duration > 0) {
            questTime = "for " + DurationFormatter.formatReadable(duration);
        } else if (startTime != null) {
            questTime = "at " + startTime;
        }

        if (TextUtils.isEmpty(questTime)) {
            rv.setViewVisibility(R.id.widget_agenda_quest_time, View.GONE);
        } else {
            rv.setViewVisibility(R.id.widget_agenda_quest_time, View.VISIBLE);
            rv.setTextViewText(R.id.widget_agenda_quest_time, questTime);
        }
        return rv;
    }

    public String getQuestName(Quest quest) {
        String name = quest.getName();
        if (quest.getRemainingCount() == 1) {
            return name;
        }
        return name + " (x" + quest.getRemainingCount() + ")";
    }

    @Override
    public RemoteViews getLoadingView() {
        return new RemoteViews(
                context.getPackageName(),
                R.layout.widget_agenda_loading_item);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
