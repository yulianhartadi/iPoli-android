package com.curiousily.ipoli.quest.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.curiousily.ipoli.DailyScheduleActivity;
import com.curiousily.ipoli.R;
import com.curiousily.ipoli.utils.TimerFormat;

import java.util.concurrent.TimeUnit;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/17/15.
 */
public class QuestService extends IntentService {

    public static final int UPDATE_PROGRESS_CODE = 5001;
    public static final int QUEST_RUNNING_NOTIFICATION_ID = 101;
    public static final int QUEST_DONE_NOTIFICATION_ID = 102;

    public QuestService() {
        super("iPoli-QuestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        updateProgress(intent);
    }

    private void finishQuest() {
        PendingIntent pendingIntent = PendingIntent.getService(this, UPDATE_PROGRESS_CODE,
                new Intent(this, getClass()), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(QUEST_RUNNING_NOTIFICATION_ID);

        Intent notificationIntent = new Intent(this, DailyScheduleActivity.class);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle("Quest complete")
                .setContentText("Ready for a sweet break?")
                .setSmallIcon(R.drawable.ic_play_arrow_white_48dp)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setContentIntent(pendingNotificationIntent)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        notificationManagerCompat.notify(QUEST_DONE_NOTIFICATION_ID, builder.build());
    }

    private void updateProgress(Intent intent) {
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        int duration = intent.getIntExtra("duration", 0);
        long startTime = intent.getLongExtra("startTime", System.currentTimeMillis());
        long now = System.currentTimeMillis();

        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(now) - TimeUnit.MILLISECONDS.toSeconds(startTime);
        int elapsedMinutes = Math.round(elapsedSeconds / 60.0f);
        int minutesRemaining = duration - elapsedMinutes;

        if (minutesRemaining <= 0) {
            finishQuest();
            return;
        }

        Intent notificationIntent = new Intent(this, DailyScheduleActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(name)
                .setContentText(description)
                .setContentInfo(TimerFormat.minutesToText(minutesRemaining))
                .setSmallIcon(R.drawable.ic_play_arrow_white_48dp)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_clear_white_24dp, "Cancel", pendingIntent)
                .addAction(R.drawable.ic_done, "Done", pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(QUEST_RUNNING_NOTIFICATION_ID, builder.build());
    }
}
