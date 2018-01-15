package io.ipoli.android.quest.receivers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.Pair;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.Random;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.QuestReminder;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.reminder.ReminderMinutesParser;
import io.ipoli.android.reminder.TimeOffsetType;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/17/16.
 */
public class RemindStartQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_REMIND_START_QUEST = "io.ipoli.android.intent.action.REMIND_START_QUEST";

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        long startTime = intent.getLongExtra(Constants.REMINDER_START_TIME, 0);
        PendingResult result = goAsync();
        questPersistenceService.findQuestRemindersAtStartTime(startTime, questReminders -> {
            for (QuestReminder qr : questReminders) {
                showNotification(context, qr);
            }
            Intent nextReminderIntent = new Intent(context, ScheduleNextRemindersReceiver.class);
            nextReminderIntent.setAction(ScheduleNextRemindersReceiver.ACTION_SCHEDULE_REMINDERS);
            context.sendBroadcast(nextReminderIntent);
            result.finish();
        });
    }

    private void showNotification(Context context, QuestReminder questReminder) {
        Intent remindStartQuestIntent = new Intent(context, QuestActivity.class);
        remindStartQuestIntent.setAction(ACTION_REMIND_START_QUEST);
        remindStartQuestIntent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questReminder.getQuestId());
        String name = questReminder.getQuestName();

        PendingIntent pendingNotificationIntent = ActivityIntentFactory.createWithParentStack(QuestActivity.class, remindStartQuestIntent, context, new Random().nextInt());

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        PendingIntent startQuestPI = getStartPendingIntent(questReminder.getQuestId(), context);
        PendingIntent snoozeQuestPI = getSnoozePendingIntent(questReminder.getQuestId(), context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(name)
                .setContentText(getContentText(context, questReminder))
                .setContentIntent(pendingNotificationIntent)
                .setShowWhen(true)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_snooze_black_24dp, context.getString(R.string.snooze).toUpperCase(), snoozeQuestPI)
                .addAction(R.drawable.ic_play_arrow_black_24dp, context.getString(R.string.start).toUpperCase(), startQuestPI)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(questReminder.getNotificationId(), builder.build());
    }

    private String getContentText(Context context, QuestReminder questReminder) {
        if (StringUtils.isEmpty(questReminder.getMessage())) {
            if (questReminder.getMinutesFromStart() == 0) {
                return context.getString(R.string.ready_to_start);
            } else {
                Pair<Long, TimeOffsetType> parseResult = ReminderMinutesParser.parseCustomMinutes(Math.abs(questReminder.getMinutesFromStart()));
                long timeValue = parseResult.first;
                TimeOffsetType timeOffsetType = parseResult.second;
                String type = timeOffsetType.name().toLowerCase();
                if (timeValue == 1) {
                    type = type.substring(0, type.length() - 1);
                }
                return "Starts in " + timeValue + " " + type;
            }
        }
        return questReminder.getMessage();
    }


    private PendingIntent getStartPendingIntent(String questId, Context context) {
        Intent intent = new Intent(context, StartQuestReceiver.class);
        intent.setAction(StartQuestReceiver.ACTION_START_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(context, intent);
    }

    private PendingIntent getSnoozePendingIntent(String questId, Context context) {
        Intent intent = new Intent(context, SnoozeQuestReceiver.class);
        intent.setAction(SnoozeQuestReceiver.ACTION_SNOOZE_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(context, intent, new Random().nextInt());
    }
}
