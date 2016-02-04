package io.ipoli.android.quest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.quest.receivers.ShowDoneQuestNotificationReceiver;
import io.ipoli.android.quest.receivers.StartQuestTimerReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class QuestNotificationScheduler {

    public static void scheduleUpdateTimer(String questId, Context context) {
        Intent intent = getQuestTimerIntent(questId, context);
        PendingIntent pendingIntent = getQuestTimerPendingIntent(context, intent);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, TimeUnit.MINUTES.toMillis(1),
                TimeUnit.MINUTES.toMillis(1), pendingIntent);
        context.sendBroadcast(intent);
    }

    public static void stopTimer(String questId, Context context) {
        cancelUpdateTimerIntent(questId, context);
        dismissTimerNotification(context);
    }

    @NonNull
    private static Intent getQuestTimerIntent(String questId, Context context) {
        Intent intent = new Intent(StartQuestTimerReceiver.ACTION_SHOW_QUEST_TIMER);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return intent;
    }

    @NonNull
    private static PendingIntent getQuestTimerPendingIntent(Context context, Intent intent) {
        return PendingIntent.getBroadcast(context, Constants.QUEST_UPDATE_TIMER_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void cancelUpdateTimerIntent(String questId, Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(getQuestTimerPendingIntent(context, getQuestTimerIntent(questId, context)));
    }

    private static void dismissTimerNotification(Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.QUEST_TIMER_NOTIFICATION_ID);
    }

    public static void scheduleDone(String questId, long scheduleTimeMillis, Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setExact(AlarmManager.RTC_WAKEUP, scheduleTimeMillis,
                getShowDonePendingIntent(questId, context));
    }

    public static void stopDone(String questId, Context context) {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.QUEST_DONE_NOTIFICATION_ID);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(getShowDonePendingIntent(questId, context));
    }

    private static PendingIntent getShowDonePendingIntent(String questId, Context context) {
        Intent intent = new Intent(ShowDoneQuestNotificationReceiver.ACTION_SHOW_DONE_QUEST_NOTIFICATION);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return PendingIntent.getBroadcast(context, Constants.QUEST_SHOW_DONE_REQUEST_CODE,
                intent, 0);
    }
}
