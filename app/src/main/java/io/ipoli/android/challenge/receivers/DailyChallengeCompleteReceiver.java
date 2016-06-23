package io.ipoli.android.challenge.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/22/16.
 */
public class DailyChallengeCompleteReceiver extends BroadcastReceiver {

    public static final String ACTION_DAILY_CHALLENGE_COMPLETE = "io.ipoli.android.intent.action.DAILY_CHALLENGE_COMPLETE";

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingIntent pendingNotificationIntent = getPendingIntent(context);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.daily_challenge_complete_title))
                .setContentText(context.getString(R.string.daily_challenge_complete_text))
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
        notificationManagerCompat.notify(Constants.DAILY_CHALLENGE_COMPLETE_NOTIFICATION_ID, builder.build());
    }

    private PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(MainActivity.ACTION_DAILY_CHALLENGE_COMPLETE);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
