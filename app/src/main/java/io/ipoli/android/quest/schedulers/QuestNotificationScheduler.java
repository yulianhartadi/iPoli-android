package io.ipoli.android.quest.schedulers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.receivers.ShowQuestCompleteNotificationReceiver;
import io.ipoli.android.quest.receivers.StartQuestTimerReceiver;
import io.ipoli.android.reminder.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class QuestNotificationScheduler {

    public static void scheduleUpdateTimer(String questId, Context context) {
        Intent intent = getQuestTimerIntent(questId, context);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                TimeUnit.MINUTES.toMillis(1), IntentUtils.getBroadcastPendingIntent(context, intent));
    }

    public static void cancelTimer(String questId, Context context) {
        cancelUpdateTimerIntent(questId, context);
        dismissTimerNotification(context);
    }

    @NonNull
    private static Intent getQuestTimerIntent(String questId, Context context) {
        Intent intent = new Intent(context, StartQuestTimerReceiver.class);
        intent.setAction(StartQuestTimerReceiver.ACTION_SHOW_QUEST_TIMER);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return intent;
    }

    private static void cancelUpdateTimerIntent(String questId, Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(IntentUtils.getBroadcastPendingIntent(context, getQuestTimerIntent(questId, context)));
    }

    private static void dismissTimerNotification(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.QUEST_TIMER_NOTIFICATION_ID);
    }

    public static void scheduleDone(String questId, long scheduleTimeMillis, Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent showDonePendingIntent = getShowDonePendingIntent(questId, context);
        if (Build.VERSION.SDK_INT > 22) {
            alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, scheduleTimeMillis, showDonePendingIntent);
        } else {
            alarm.setExact(AlarmManager.RTC_WAKEUP, scheduleTimeMillis, showDonePendingIntent);
        }
    }

    public static void cancelDone(String questId, Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.QUEST_COMPLETE_NOTIFICATION_ID);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(getShowDonePendingIntent(questId, context));
    }

    private static PendingIntent getShowDonePendingIntent(String questId, Context context) {
        Intent intent = new Intent(context, ShowQuestCompleteNotificationReceiver.class);
        intent.setAction(ShowQuestCompleteNotificationReceiver.ACTION_SHOW_DONE_QUEST_NOTIFICATION);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(context, intent);
    }

    public static void cancelReminders(List<Reminder> reminders, Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        for (Reminder reminder : reminders) {
            notificationManagerCompat.cancel(reminder.getNotificationNum());
        }
    }

    public static void cancelAll(Quest quest, Context context) {
        cancelDone(quest.getId(), context);
        cancelTimer(quest.getId(), context);
        cancelReminders(quest.getReminders(), context);
    }
}
