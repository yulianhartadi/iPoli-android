package io.ipoli.android.app.receivers;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import io.ipoli.android.app.sync.AndroidCalendarSyncJobService;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/8/16.
 */
public class AndroidCalendarEventChangedReceiver extends BroadcastReceiver {

    private static final int SYNC_CALENDAR_JOB_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!EasyPermissions.hasPermissions(context, Manifest.permission.READ_CALENDAR)) {
            return;
        }
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo jobInfo = new JobInfo.Builder(SYNC_CALENDAR_JOB_ID,
                new ComponentName(context, AndroidCalendarSyncJobService.class))
                .build();
        jobScheduler.schedule(jobInfo);
    }
}