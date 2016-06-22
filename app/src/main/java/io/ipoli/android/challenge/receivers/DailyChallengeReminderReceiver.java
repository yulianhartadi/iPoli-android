package io.ipoli.android.challenge.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import org.joda.time.LocalDate;

import java.util.Set;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.challenge.activities.PickDailyChallengeQuestsActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_DAILY_CHALLENGE_REMINDER = "io.ipoli.android.intent.action.SHOW_DAILY_CHALLENGE_REMINDER";

    @Override
    public void onReceive(Context context, Intent intent) {

        LocalStorage localStorage = LocalStorage.of(context);

        boolean isReminderEnabled = localStorage.readBool(Constants.KEY_DAILY_CHALLENGE_ENABLE_REMINDER, Constants.DEFAULT_DAILY_CHALLENGE_ENABLE_REMINDER);
        if (!isReminderEnabled) {
            return;
        }

        int currentDayOfWeek = LocalDate.now().getDayOfWeek();
        Set<Integer> daysOfWeek = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        if (!daysOfWeek.contains(currentDayOfWeek)) {
            return;
        }

        Intent pickDailyChallengeQuestsIntent = new Intent(context, PickDailyChallengeQuestsActivity.class);

        PendingIntent pendingNotificationIntent = ActivityIntentFactory.createWithParentStack(PickDailyChallengeQuestsActivity.class, pickDailyChallengeQuestsIntent, context);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle("Do you accept or refuse your daily challenge?")
                .setContentText("Pick your 3 most important quests that will make the most impact on your day")
                .setContentIntent(pendingNotificationIntent)
                .setShowWhen(true)
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.REMIND_START_QUEST_NOTIFICATION_ID, builder.build());
    }
}
