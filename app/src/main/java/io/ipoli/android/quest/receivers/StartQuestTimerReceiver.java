package io.ipoli.android.quest.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StartQuestTimerReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_QUEST_TIMER = "io.ipoli.android.intent.action.SHOW_QUEST_TIMER";

    private Context context;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        App.getAppComponent(context).inject(this);

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        Quest q = questPersistenceService.findById(questId);
        showQuestTimerNotification(q);
    }

    private void showQuestTimerNotification(Quest q) {
        int duration = q.getDuration();

        long startTime = q.getActualStartDateTime().getTime();
        long now = System.currentTimeMillis();
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(now) - TimeUnit.MILLISECONDS.toSeconds(startTime);
        int elapsedMinutes = Math.round(elapsedSeconds / 60.0f);

        NotificationCompat.Builder builder = getNotificationBuilder(q, elapsedMinutes);
        builder.setContentText("Are you focused?");
        builder.setContentIntent(getContentIntent(q.getId()));
        builder.addAction(R.drawable.ic_clear_24dp, "Cancel", getPendingIntent(q.getId(), QuestActivity.ACTION_QUEST_CANCELED));
        builder.addAction(R.drawable.ic_done_24dp, "Done", getPendingIntent(q.getId(), QuestActivity.ACTION_QUEST_DONE));
        if (duration > 0) {
            long hours = TimeUnit.MINUTES.toHours(duration);
            int minutesRemaining = duration - elapsedMinutes;
            if (minutesRemaining > 0) {
                long minutes = duration - hours * 60;
                builder.setContentText(hours > 0 ?
                        context.getString(R.string.quest_timer_hours_and_minutes, hours, minutes) :
                        context.getString(R.string.quest_timer_minutes, minutes));
            }
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.QUEST_TIMER_NOTIFICATION_ID, builder.build());
    }

    private NotificationCompat.Builder getNotificationBuilder(Quest q, int elapsedMinutes) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(q.getName())
                .setContentInfo(elapsedMinutes + " m")
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setWhen(q.getActualStartDateTime().getTime())
                .setOnlyAlertOnce(true)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
    }

    private PendingIntent getContentIntent(String questId) {
        Intent intent = new Intent(context, QuestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private PendingIntent getPendingIntent(String questId, String action) {
        Intent intent = new Intent(context, QuestActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(action);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }
}
