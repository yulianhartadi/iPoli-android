package io.ipoli.android.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/15.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}