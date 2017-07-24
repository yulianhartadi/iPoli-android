package io.ipoli.android.achievement;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

import io.ipoli.android.Constants;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class UnlockAchievementScheduler {

    public static void scheduleFindUnlocked(Context context, AchievementAction action) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        PersistableBundle data = new PersistableBundle();
        data.putString(Constants.KEY_ACHIEVEMENT_ACTION, action.name());
        JobInfo job = new JobInfo.Builder(AchievementUnlockJobService.JOB_ID,
                new ComponentName(context, AchievementUnlockJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .setMinimumLatency(2000)
                .setOverrideDeadline(3000)
                .setPersisted(true)
                .setExtras(data)
                .build();
        jobScheduler.schedule(job);
    }
}
