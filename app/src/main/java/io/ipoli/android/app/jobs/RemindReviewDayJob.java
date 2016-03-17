package io.ipoli.android.app.jobs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.ipoli.android.app.receivers.ReviewDayReceiver;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class RemindReviewDayJob extends RemindJob {

    private final Context context;

    public RemindReviewDayJob(Context context, Time time) {
        super(time);
        this.context = context;
    }

    @Override
    protected void execute(long timeToSchedule) {
        Intent i = new Intent(context, ReviewDayReceiver.class);
        PendingIntent pendingIntent = IntentUtils.getBroadcastPendingIntent(context, i);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, timeToSchedule, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}
