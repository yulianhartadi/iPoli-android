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
import io.ipoli.android.reminder.data.Reminder;

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
        App.getAppComponent(context).inject(this);

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, q -> {

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            for (Reminder r : q.getReminders()) {
                notificationManagerCompat.cancel(r.getNotificationNum());
            }

            q.setStartMinute(q.getStartMinute() + Constants.DEFAULT_SNOOZE_TIME_MINUTES);
            questPersistenceService.save(q);
            eventBus.post(new QuestSnoozedEvent(q));
            result.finish();
        });
    }
}
