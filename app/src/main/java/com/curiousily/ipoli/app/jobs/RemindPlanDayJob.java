package com.curiousily.ipoli.app.jobs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.curiousily.ipoli.app.services.ReminderIntentService;
import com.curiousily.ipoli.utils.Time;

import java.util.Calendar;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/15.
 */
public class RemindPlanDayJob {

    private final Context context;
    private final Time time;

    public RemindPlanDayJob(Context context, Time time) {
        this.context = context;
        this.time = time;
    }

    public void schedule() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, time.hour);
        calendar.set(Calendar.MINUTE, time.minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent serviceIntent = new Intent(context, ReminderIntentService.class);
        serviceIntent.setAction(ReminderIntentService.ACTION_REMIND_PLAN_DAY);
        PendingIntent pendingIntent = PendingIntent.getService(context,
                0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
