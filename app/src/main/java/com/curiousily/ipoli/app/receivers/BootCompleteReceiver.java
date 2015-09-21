package com.curiousily.ipoli.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.curiousily.ipoli.Constants;
import com.curiousily.ipoli.app.jobs.RemindPlanDayJob;
import com.curiousily.ipoli.utils.Time;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/15.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Time time = Time.of(Constants.DEFAULT_PLAN_DAY_TIME);
        new RemindPlanDayJob(context, time).schedule();
    }
}
