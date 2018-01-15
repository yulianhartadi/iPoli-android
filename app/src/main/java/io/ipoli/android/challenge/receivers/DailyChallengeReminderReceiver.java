package io.ipoli.android.challenge.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import org.threeten.bp.LocalDate;

import java.util.Date;
import java.util.Set;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.challenge.activities.PickDailyChallengeQuestsActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeReminderReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_DAILY_CHALLENGE_REMINDER = "io.ipoli.android.intent.action.SHOW_DAILY_CHALLENGE_REMINDER";

    @Inject
    LocalStorage localStorage;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        Date todayUtc = DateUtils.toStartOfDayUTC(LocalDate.now());
        Date lastCompleted = new Date(localStorage.readLong(Constants.KEY_DAILY_CHALLENGE_LAST_COMPLETED));
        boolean isCompletedForToday = todayUtc.equals(lastCompleted);
        if (isCompletedForToday) {
            return;
        }

        boolean isReminderEnabled = localStorage.readBool(Constants.KEY_DAILY_CHALLENGE_ENABLE_REMINDER, Constants.DEFAULT_DAILY_CHALLENGE_ENABLE_REMINDER);
        if (!isReminderEnabled) {
            return;
        }

        int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        Set<Integer> challengeDays = localStorage.readIntSet(Constants.KEY_DAILY_CHALLENGE_DAYS, Constants.DEFAULT_DAILY_CHALLENGE_DAYS);
        if (!challengeDays.contains(currentDayOfWeek)) {
            return;
        }

        Intent pickDailyChallengeQuestsIntent = new Intent(context, PickDailyChallengeQuestsActivity.class);

        PendingIntent pendingNotificationIntent = ActivityIntentFactory.createWithParentStack(PickDailyChallengeQuestsActivity.class, pickDailyChallengeQuestsIntent, context);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        String text = context.getString(R.string.daily_challenge_notification_text);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(context.getString(R.string.daily_challenge_notification_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
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
        notificationManagerCompat.notify(Constants.REMIND_DAILY_CHALLENGE_NOTIFICATION_ID, builder.build());
    }
}
