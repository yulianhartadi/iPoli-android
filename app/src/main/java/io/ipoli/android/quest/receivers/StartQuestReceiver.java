package io.ipoli.android.quest.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.services.QuestTimerIntentService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class StartQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_START_QUEST = "io.ipoli.android.intent.action.START_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.REMIND_START_QUEST_NOTIFICATION_ID);
        Quest q = getQuest(intent);
        q.setStatus(Status.STARTED.name());
        q.setStartTime(new Date());
        questPersistenceService.save(q);
        scheduleUpdateQuestTimer(q.getId(), context);
        scheduleNextQuestReminder(context);
    }

    private Quest getQuest(Intent intent) {
        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        return questPersistenceService.findById(questId);
    }

    private void scheduleUpdateQuestTimer(String questId, Context context) {
        Intent intent = new Intent(context, QuestTimerIntentService.class);
        intent.setAction(QuestTimerIntentService.ACTION_SHOW_QUEST_TIMER);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        PendingIntent pendingIntent = PendingIntent.getService(context, Constants.QUEST_UPDATE_TIMER_REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(1), pendingIntent);
        context.startService(intent);
    }

    private void scheduleNextQuestReminder(Context context) {
        context.sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}
