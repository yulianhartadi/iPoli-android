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

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class ShowQuestCompleteNotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_DONE_QUEST_NOTIFICATION = "io.ipoli.android.intent.action.SHOW_QUEST_COMPLETE_NOTIFICATION";

    @Inject
    Bus eventBus;

    @Inject
    QuestPersistenceService questPersistenceService;

    private NotificationCompat.Builder createNotificationBuilder(Context context, Quest q) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle(q.getName())
                .setContentText("Quest done! Ready for a break?")
                .setContentIntent(getPendingIntent(context, q.getId()))
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setWhen(q.getActualStartDate().getTime())
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
    }

    private PendingIntent getPendingIntent(Context context, String questId) {
        Intent intent = new Intent(context, CompleteQuestReceiver.class);
        intent.setAction(CompleteQuestReceiver.ACTION_COMPLETE_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        PendingResult result = goAsync();
        App.getAppComponent(context).inject(this);
        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        QuestNotificationScheduler.cancelTimer(questId, context);
        questPersistenceService.findById(questId, q -> {
            NotificationCompat.Builder builder = createNotificationBuilder(context, q);
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            notificationManagerCompat.notify(Constants.QUEST_COMPLETE_NOTIFICATION_ID, builder.build());
            result.finish();
        });
    }
}
