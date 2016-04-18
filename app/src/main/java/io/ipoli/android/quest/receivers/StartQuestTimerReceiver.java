package io.ipoli.android.quest.receivers;

import android.app.PendingIntent;
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
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.app.receivers.AsyncBroadcastReceiver;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StartQuestTimerReceiver extends AsyncBroadcastReceiver {

    public static final String ACTION_SHOW_QUEST_TIMER = "io.ipoli.android.intent.action.SHOW_QUEST_TIMER";

    private Context context;

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    protected Observable<Void> doOnReceive(Context context, Intent intent) {
        this.context = context;
        App.getAppComponent(context).inject(this);

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        return questPersistenceService.findById(questId).flatMap(q -> {
            showQuestTimerNotification(q);
            return Observable.empty();
        });
    }

    private void showQuestTimerNotification(Quest q) {
        int duration = q.getDuration();

        long startTime = q.getActualStart().getTime();
        long now = System.currentTimeMillis();
        long elapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(now) - TimeUnit.MILLISECONDS.toSeconds(startTime);
        int elapsedMinutes = Math.round(elapsedSeconds / 60.0f);

        NotificationCompat.Builder builder = getNotificationBuilder(q, elapsedMinutes);
        builder.setContentText("Are you focused?");
        builder.setContentIntent(getContentIntent(q.getId()));
        builder.addAction(R.drawable.ic_clear_24dp, "Cancel", getPendingIntent(q.getId(), QuestActivity.ACTION_QUEST_CANCELED));
        builder.addAction(R.drawable.ic_done_24dp, "Done", getPendingIntent(q.getId(), QuestActivity.ACTION_QUEST_DONE));
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
                .setWhen(q.getActualStart().getTime())
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

    private PendingIntent getPendingIntent(String questId, String action) {
        Intent intent = new Intent(context, QuestActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(action);

        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, intent, context);
    }
}
