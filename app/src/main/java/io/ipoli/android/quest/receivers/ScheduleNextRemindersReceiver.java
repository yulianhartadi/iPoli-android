package io.ipoli.android.quest.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/1/16.
 */
public class ScheduleNextRemindersReceiver extends BroadcastReceiver {

    public static final String ACTION_SCHEDULE_REMINDERS = "io.ipoli.android.intent.action.SCHEDULE_QUEST_REMINDERS";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        PendingResult result = goAsync();
        questPersistenceService.findNextReminderTime(nextReminderTime -> {
            Intent i = new Intent(context, RemindStartQuestReceiver.class);
            i.setAction(RemindStartQuestReceiver.ACTION_REMIND_START_QUEST);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(IntentUtils.getBroadcastPendingIntent(context, i));
            if (nextReminderTime == null) {
                result.finish();
                return;
            }
            i.putExtra(Constants.REMINDER_START_TIME, nextReminderTime);
            PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(context, i);
            if (Build.VERSION.SDK_INT > 22) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextReminderTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextReminderTime, pendingIntent);
            }
            result.finish();
        });
    }
}
