package io.ipoli.android.quest.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestListActivity;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

public class QuestTimerIntentService extends IntentService {
    public static final String ACTION_SHOW_QUEST_TIMER = "io.ipoli.android.action.SHOW_QUEST_TIMER";

    @Inject
    QuestPersistenceService questPersistenceService;

    public QuestTimerIntentService() {
        super("iPoli-QuestTimerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            Log.d("iPoli", getClass().getSimpleName() + " was started without intent or action");
            return;
        }
        String action = intent.getAction();
        App.getAppComponent(this).inject(this);
        if(action.equals(ACTION_SHOW_QUEST_TIMER)) {
            String questId = intent.getStringExtra("id");
            Quest q = questPersistenceService.findById(questId);
            showQuestStartedNotification(q);
        }
    }

    private void showQuestStartedNotification(Quest q) {
        String name = q.getName();
        int duration = q.getDuration();

        long startTime = q.getStartTime().getTime();
        long now = System.currentTimeMillis();
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(now) - TimeUnit.MILLISECONDS.toSeconds(startTime);
        int elapsedMinutes = Math.round(elapsedSeconds / 60.0f);

        if(duration > 0) {
            int minutesRemaining = duration - elapsedMinutes;
            if(minutesRemaining <= 0) {
                //finish quest
                return;
            }
        }

        Intent doneIntent = new Intent(this, QuestListActivity.class);
        doneIntent.putExtra("id", q.getId());
        doneIntent.setAction(QuestListActivity.ACTION_QUEST_DONE);
        PendingIntent donePI = PendingIntent.getActivity(this, 0, doneIntent, PendingIntent.FLAG_ONE_SHOT);

        Intent cancelIntent = new Intent(this, QuestListActivity.class);
        cancelIntent.putExtra("id", q.getId());
        cancelIntent.setAction(QuestListActivity.ACTION_QUEST_CANCELED);
        PendingIntent cancelPI = PendingIntent.getActivity(this, 0, cancelIntent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
        String description = "";

        if(duration > 0) {
            long hours = TimeUnit.MINUTES.toHours(duration);
            long minutes = duration - hours * 60;

            if (hours > 0) {
                description = String.format("For %d hour(s) and %02d minutes", hours, minutes);
            } else {
                description = String.format("For %d minutes", minutes);
            }
        }

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(name)
                .setContentText(description)
                .setContentInfo(elapsedMinutes + " m")
                .setSmallIcon(R.drawable.ic_tag_faces_white_48dp)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .addAction(R.drawable.ic_clear_24dp, "Cancel", cancelPI)
                .addAction(R.drawable.ic_done_24dp, "Done", donePI)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.QUEST_STARTED_NOTIFICATION_ID, builder.build());
    }

}
