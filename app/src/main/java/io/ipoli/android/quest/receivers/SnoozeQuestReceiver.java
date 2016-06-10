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
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class SnoozeQuestReceiver extends AsyncBroadcastReceiver {

    public static final String ACTION_SNOOZE_QUEST = "io.ipoli.android.intent.action.SNOOZE_QUEST";

    @Inject
    Bus eventBus;

    QuestPersistenceService questPersistenceService;

    @Override
    protected Observable<Void> doOnReceive(Context context, Intent intent) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.REMIND_START_QUEST_NOTIFICATION_ID);

        App.getAppComponent(context).inject(this);

        questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);

        return getQuest(intent).flatMap(q -> {
            q.setStartMinute(q.getStartMinute() + Constants.DEFAULT_SNOOZE_TIME_MINUTES);
            return questPersistenceService.save(q).flatMap(quest -> {
                scheduleNextQuestReminder(context);
                eventBus.post(new QuestSnoozedEvent(q));
                return Observable.empty();
            });
        });
    }

    private Observable<Quest> getQuest(Intent intent) {
        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        return questPersistenceService.findById(questId);
    }

    private void scheduleNextQuestReminder(Context context) {
        context.sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}
