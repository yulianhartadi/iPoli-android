package io.ipoli.android.achievement;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.ipoli.android.Constants;
import io.ipoli.android.achievement.actions.AchievementAction;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class UnlockAchievementScheduler {

    public static void scheduleFindUnlocked(Context context, AchievementAction action) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        PersistableBundle data = new PersistableBundle();
        data.putString(Constants.KEY_ACHIEVEMENT_ACTION_CLASS, action.getClass().getName());
        try {
            data.putString(Constants.KEY_ACHIEVEMENT_ACTION, new ObjectMapper().writeValueAsString(action));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        JobInfo job = new JobInfo.Builder(AchievementUnlockJobService.JOB_ID,
                new ComponentName(context, AchievementUnlockJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .setMinimumLatency(1000)
                .setOverrideDeadline(2000)
                .setExtras(data)
                .build();
        jobScheduler.schedule(job);
    }
}
