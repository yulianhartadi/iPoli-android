package io.ipoli.android.app.jobs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.ipoli.android.app.services.ReminderIntentService;
import io.ipoli.android.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/15.
 */
public class RemindPlanDayJob extends RemindJob {

    private final Context context;

    public RemindPlanDayJob(Context context, Time time) {
        super(time);
        this.context = context;
    }

    @Override
    protected void execute(long timeToSchedule) {
        Intent serviceIntent = new Intent(context, ReminderIntentService.class);
        serviceIntent.setAction(ReminderIntentService.ACTION_REMIND_PLAN_DAY);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, timeToSchedule, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}