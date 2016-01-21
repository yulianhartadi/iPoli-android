package io.ipoli.android.quest.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.ScheduleNextQuestReminderEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/21/16.
 */
public class UpdateQuestIntentService extends IntentService {

    public static final String ACTION_START_QUEST = "io.ipoli.android.action.START_QUEST";
    public static final String ACTION_SNOOZE_QUEST = "io.ipoli.android.action.SNOOZE_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    Bus eventBus;

    public UpdateQuestIntentService() {
        super("iPoli-UpdateQuestIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            Log.d("iPoli", getClass().getSimpleName() + " was started without intent or action");
            return;
        }

        String action = intent.getAction();
        App.getAppComponent(this).inject(this);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(Constants.REMIND_START_QUEST_NOTIFICATION_ID);

        if (action.equals(ACTION_START_QUEST)) {
            Quest q = getQuest(intent);
            q.setStatus(Quest.Status.STARTED.name());
            q.setStartTime(new Date());
            questPersistenceService.save(q);
            scheduleUpdateQuestTimer(q.getId());
            scheduleNextQuestReminder();
        } else if (action.equals(ACTION_SNOOZE_QUEST)) {
            Quest q = getQuest(intent);
            Date startTime = q.getStartTime();
            Calendar c = Calendar.getInstance();
            c.setTime(startTime);
            c.add(Calendar.MINUTE, Constants.DEFAULT_SNOOZE_TIME_MINUTES);
            q.setStartTime(c.getTime());
            questPersistenceService.save(q);
            scheduleNextQuestReminder();
        }
    }

    private void scheduleNextQuestReminder() {
        eventBus.post(new ScheduleNextQuestReminderEvent());
    }

    private Quest getQuest(Intent intent) {
        String questId = intent.getStringExtra("id");
        return questPersistenceService.findById(questId);
    }

    private void scheduleUpdateQuestTimer(String questId) {
        Intent intent = new Intent(this, QuestTimerIntentService.class);
        intent.setAction(QuestTimerIntentService.ACTION_SHOW_QUEST_TIMER);
        intent.putExtra("id", questId);
        PendingIntent pendingIntent = PendingIntent.getService(this, Constants.QUEST_UPDATE_TIMER_REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(1), pendingIntent);
        startService(intent);
    }

}
