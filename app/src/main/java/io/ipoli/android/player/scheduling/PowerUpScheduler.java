package io.ipoli.android.player.scheduling;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.player.PowerUpsJobService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/17/17.
 */
public class PowerUpScheduler {

    public static void scheduleExpirationCheckJob(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(PowerUpsJobService.JOB_ID);
        JobInfo job = new JobInfo.Builder(PowerUpsJobService.JOB_ID, new ComponentName(context, PowerUpsJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(TimeUnit.DAYS.toMillis(1))
                .setPersisted(true)
                .build();
        jobScheduler.schedule(job);
    }
}
