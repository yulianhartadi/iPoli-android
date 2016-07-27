package io.ipoli.android.quest.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.quest.data.Reminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/1/16.
 */
public class ScheduleNextRemindersReceiver extends BroadcastReceiver {

    public static final String ACTION_SCHEDULE_REMINDERS = "io.ipoli.android.intent.action.SCHEDULE_QUEST_REMINDERS";

    @Override
    public void onReceive(Context context, Intent intent) {
        List<Reminder> reminders = getReminders();
        Intent i = new Intent(RemindStartQuestReceiver.ACTION_REMIND_START_QUEST);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(IntentUtils.getBroadcastPendingIntent(context, i));
        if (reminders.isEmpty()) {
            return;
        }
        i.putStringArrayListExtra(Constants.REMINDER_IDS_EXTRA_KEY, getReminderIds(reminders));
        PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(context, i);
        long alarmTime = reminders.get(0).getStartTime().getTime();
        if (Build.VERSION.SDK_INT > 22) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
        }
    }

    private List<Reminder> getReminders() {
        return new ArrayList<>();
    }

    @NonNull
    private ArrayList<String> getReminderIds(List<Reminder> reminders) {
        ArrayList<String> reminderIds = new ArrayList<>();
        for (Reminder reminder : reminders) {
            reminderIds.add(reminder.getId());
        }
        return reminderIds;
    }
}
