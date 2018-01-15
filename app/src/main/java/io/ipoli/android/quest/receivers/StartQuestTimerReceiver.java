package io.ipoli.android.quest.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.squareup.otto.Bus;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.app.ui.formatters.DurationFormatter;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

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
            if (q.isStarted()) {
                showQuestTimerNotification(q);
            }
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
        builder.addAction(R.drawable.ic_clear_black_24dp, context.getString(R.string.cancel), getCancelPendingIntent(q.getId()));
        builder.addAction(R.drawable.ic_done_24dp, context.getString(R.string.done), getDonePendingIntent(q.getId()));
        if (duration > 0) {
            builder.setContentText("For " + DurationFormatter.format(context, duration));
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.QUEST_TIMER_NOTIFICATION_ID, builder.build());
    }

    private NotificationCompat.Builder getNotificationBuilder(Quest q, int elapsedMinutes) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
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

    private PendingIntent getCancelPendingIntent(String questId) {
        Intent intent = new Intent(context, StopQuestReceiver.class);
        intent.setAction(StopQuestReceiver.ACTION_STOP_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(context, intent);
    }

    private PendingIntent getDonePendingIntent(String questId) {
        Intent intent = new Intent(context, CompleteQuestReceiver.class);
        intent.setAction(CompleteQuestReceiver.ACTION_COMPLETE_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(context, intent);
    }
}
