package io.ipoli.android.quest.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.events.QuestSnoozedEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class SnoozeQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_SNOOZE_QUEST = "io.ipoli.android.intent.action.SNOOZE_QUEST";

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {

        PendingResult result = goAsync();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(intent.getIntExtra(Constants.REMINDER_NOTIFICATION_ID_EXTRA_KEY, 0));

        App.getAppComponent(context).inject(this);

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, q -> {
            q.setStartMinute(q.getStartMinute() + Constants.DEFAULT_SNOOZE_TIME_MINUTES);
            questPersistenceService.save(q);
            eventBus.post(new QuestSnoozedEvent(q));
            result.finish();
        });
    }
}
