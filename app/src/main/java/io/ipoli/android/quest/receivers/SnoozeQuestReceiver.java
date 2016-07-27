package io.ipoli.android.quest.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.receivers.AsyncBroadcastReceiver;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class SnoozeQuestReceiver extends AsyncBroadcastReceiver {

    public static final String ACTION_SNOOZE_QUEST = "io.ipoli.android.intent.action.SNOOZE_QUEST";

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    protected Observable<Void> doOnReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(intent.getIntExtra(Constants.REMINDER_NOTIFICATION_ID_EXTRA_KEY, 0));

        App.getAppComponent(context).inject(this);

        Quest q = getQuest(intent);
        q.setStartMinute(q.getStartMinute() + Constants.DEFAULT_SNOOZE_TIME_MINUTES);
        questPersistenceService.save(q);
        eventBus.post(new QuestSnoozedEvent(q));
        return Observable.empty();
    }

    private Quest getQuest(Intent intent) {
        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        return questPersistenceService.findById(questId);
    }
}
