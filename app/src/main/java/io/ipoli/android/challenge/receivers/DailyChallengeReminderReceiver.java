package io.ipoli.android.challenge.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_DAILY_CHALLENGE_REMINDER = "io.ipoli.android.intent.action.SHOW_DAILY_CHALLENGE_REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {

        // check if should show notification && should challenge today
    }
}
