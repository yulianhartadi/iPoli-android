package io.ipoli.android.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import io.ipoli.android.app.App;
import io.ipoli.android.challenge.receivers.ScheduleDailyChallengeReminderReceiver;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.receivers.ScheduleNextRemindersReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/15.
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        if (playerPersistenceService.get() == null) {
            return;
        }
        Intent nextReminderIntent = new Intent(context, ScheduleNextRemindersReceiver.class);
        nextReminderIntent.setAction(ScheduleNextRemindersReceiver.ACTION_SCHEDULE_REMINDERS);
        context.sendBroadcast(nextReminderIntent);


        Intent dailyChallengeIntent = new Intent(context, ScheduleDailyChallengeReminderReceiver.class);
        dailyChallengeIntent.setAction(ScheduleDailyChallengeReminderReceiver.ACTION_SCHEDULE_DAILY_CHALLENGE_REMINDER);
        context.sendBroadcast(dailyChallengeIntent);
    }
}