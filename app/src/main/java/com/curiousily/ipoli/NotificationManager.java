package com.curiousily.ipoli;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.curiousily.ipoli.models.Quest;
import com.curiousily.ipoli.utils.TimerFormat;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/12/15.
 */
public class NotificationManager {

    private final Context context;

    public NotificationManager(Context context) {
        this.context = context;
    }

    public static NotificationManager from(Context context) {
        return new NotificationManager(context);
    }

    public void startQuest(Quest quest) {
        Intent notificationIntent = new Intent(context, DailyScheduleActivity.class);

        PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        final NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(quest.name)
                .setContentText(TimerFormat.minutesToText(quest.duration))
                .setContentInfo(quest.name)
                .setSmallIcon(R.drawable.ic_event)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_discuss, "Cancel", intent)
                .addAction(R.drawable.ic_done, "Done", intent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, builder.build());

        new CountDownTimer(600000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                builder.setContentText(TimerFormat.millisecondsToText(millisUntilFinished));
                notificationManagerCompat.notify(1, builder.build());
            }

            @Override
            public void onFinish() {

            }
        }.start();
    }
}
