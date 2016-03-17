package io.ipoli.android.app.jobs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.ipoli.android.app.receivers.PlanDayReceiver;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.Time;

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
        Intent i = new Intent(context, PlanDayReceiver.class);
        PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(context, i);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, timeToSchedule, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}