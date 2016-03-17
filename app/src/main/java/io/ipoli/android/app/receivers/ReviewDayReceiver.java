package io.ipoli.android.app.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.MainActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/17/16.
 */
public class ReviewDayReceiver extends BroadcastReceiver {

    public static final String ACTION_REMIND_REVIEW_DAY = "io.ipoli.android.intent.action.REMIND_REVIEW_DAY";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent reviewDayIntent = new Intent(context, MainActivity.class);
        reviewDayIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        reviewDayIntent.setAction(ACTION_REMIND_REVIEW_DAY);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(context, 0, reviewDayIntent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.review_day_title))
                .setContentText(context.getString(R.string.review_day_text))
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(pendingNotificationIntent)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.REMIND_REVIEW_DAY_NOTIFICATION_ID, builder.build());
    }
}
