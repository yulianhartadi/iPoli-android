package io.ipoli.android.quest.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StartQuestTimerReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_QUEST_TIMER = "io.ipoli.android.intent.action.SHOW_QUEST_TIMER";

    private Context context;

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult result = goAsync();
        this.context = context;
        App.getAppComponent(context).inject(this);

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, q -> {
            showQuestTimerNotification(q);
            result.finish();
        });
    }

    private void showQuestTimerNotification(Quest q) {
        int duration = q.getDuration();

        long startTime = q.getActualStartDate().getTime();
        long now = System.currentTimeMillis();
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(now) - TimeUnit.MILLISECONDS.toSeconds(startTime);
        int elapsedMinutes = Math.round(elapsedSeconds / 60.0f);

        NotificationCompat.Builder builder = getNotificationBuilder(q, elapsedMinutes);
        builder.setContentText("Are you focused?");
        builder.setContentIntent(getContentIntent(q.getId()));
        builder.addAction(R.drawable.ic_clear_24dp, "Cancel", getCancelPendingIntent(q.getId(), QuestActivity.ACTION_QUEST_CANCELED));
        builder.addAction(R.drawable.ic_done_24dp, "Done", getDonePendingIntent(q.getId(), MainActivity.ACTION_QUEST_COMPLETE));
        if (duration > 0) {
            builder.setContentText("For " + DurationFormatter.format(context, duration));
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
                .setWhen(q.getActualStartDate().getTime())
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
    }

    private PendingIntent getContentIntent(String questId) {

        Intent intent = new Intent(context, QuestActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);

        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, intent, context);
    }

    private PendingIntent getCancelPendingIntent(String questId, String action) {
        Intent intent = new Intent(context, QuestActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(action);

        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, intent, context);
    }

    private PendingIntent getDonePendingIntent(String questId, String action) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(action);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
