package io.ipoli.android.app.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.chat.ChatActivity;
import io.ipoli.android.quest.PlanDayActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/15.
 */
public class ReminderIntentService extends IntentService {

    public static final String ACTION_REMIND_PLAN_DAY = "io.ipoli.android.action.REMIND_PLAN_DAY";
    public static final String ACTION_REMIND_REVIEW_DAY = "io.ipoli.android.action.REMIND_REVIEW_DAY";

    public ReminderIntentService() {
        super("iPoli-ReminderIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String action = intent.getAction();
        if (action.equals(ACTION_REMIND_PLAN_DAY)) {

            Intent planDayIntent = new Intent(this, PlanDayActivity.class);
            planDayIntent.setAction(ACTION_REMIND_PLAN_DAY);

            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, planDayIntent, PendingIntent.FLAG_ONE_SHOT);

            Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setContentTitle("New day ahead")
                    .setContentText("Ready to plan your great day?")
                    .setSmallIcon(R.drawable.ic_tag_faces_white_48dp)
                    .setLargeIcon(largeIcon)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingNotificationIntent)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(Constants.REMIND_PLAN_DAY_NOTIFICATION_ID, builder.build());
        } else if (action.equals(ACTION_REMIND_REVIEW_DAY)) {

            Intent reviewDayIntent = new Intent(this, ChatActivity.class);
            reviewDayIntent.setAction(ACTION_REMIND_REVIEW_DAY);

            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, reviewDayIntent, PendingIntent.FLAG_ONE_SHOT);

            Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                    .setContentTitle("Day in review")
                    .setContentText("Ready to review your day?")
                    .setSmallIcon(R.drawable.ic_tag_faces_white_48dp)
                    .setLargeIcon(largeIcon)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(pendingNotificationIntent)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(Constants.REMIND_REVIEW_DAY_NOTIFICATION_ID, builder.build());
        }

    }
}