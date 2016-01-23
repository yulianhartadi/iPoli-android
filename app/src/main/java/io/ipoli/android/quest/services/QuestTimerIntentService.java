package io.ipoli.android.quest.services;

import android.app.AlarmManager;
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
        if (action.equals(ACTION_SHOW_QUEST_TIMER)) {
            String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
            Quest q = questPersistenceService.findById(questId);
            showQuestTimerNotification(q, intent);
        }
    }

    private void showQuestTimerNotification(Quest q, Intent intent) {
        int duration = q.getDuration();

        long startTime = q.getStartTime().getTime();
        long now = System.currentTimeMillis();
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(now) - TimeUnit.MILLISECONDS.toSeconds(startTime);
        int elapsedMinutes = Math.round(elapsedSeconds / 60.0f);

        NotificationCompat.Builder builder = getNotificationBuilder(q, elapsedMinutes);
        builder.setContentText("Are you focused?");
        if (duration > 0) {

            long hours = TimeUnit.MINUTES.toHours(duration);
            int minutesRemaining = duration - elapsedMinutes;
            if (minutesRemaining > 0) {
                long minutes = duration - hours * 60;
                builder.setContentText(hours > 0 ?
                        String.format("For %d hour(s) and %02d minutes", hours, minutes) :
                        String.format("For %d minutes", minutes));
                builder.addAction(R.drawable.ic_clear_24dp, "Cancel", getPendingIntent(q, QuestListActivity.ACTION_QUEST_CANCELED));
                builder.addAction(R.drawable.ic_done_24dp, "Done", getPendingIntent(q, QuestListActivity.ACTION_QUEST_DONE));
            } else {
                builder.setOnlyAlertOnce(false);
                builder.setContentIntent(getPendingIntent(q, QuestListActivity.ACTION_QUEST_DONE));
                builder.setContentText("Quest done! Ready for a break?");
                PendingIntent pendingIntent = PendingIntent.getService(this, Constants.QUEST_UPDATE_TIMER_REQUEST_CODE,
                        intent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                alarm.cancel(pendingIntent);
            }
        }


        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(Constants.QUEST_TIMER_NOTIFICATION_ID, builder.build());
    }

    private NotificationCompat.Builder getNotificationBuilder(Quest q, int elapsedMinutes) {
        Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher);
        return (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setContentTitle(q.getName())
                .setContentInfo(elapsedMinutes + " m")
                .setSmallIcon(R.drawable.ic_tag_faces_white_48dp)
                .setLargeIcon(largeIcon)
                .setWhen(q.getStartTime().getTime())
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
    }

    private PendingIntent getPendingIntent(Quest q, String action) {
        Intent doneIntent = new Intent(this, QuestListActivity.class);
        doneIntent.putExtra(Constants.QUEST_ID_EXTRA_KEY, q.getId());
        doneIntent.setAction(action);
        return PendingIntent.getActivity(this, 0, doneIntent, PendingIntent.FLAG_ONE_SHOT);
    }

}
