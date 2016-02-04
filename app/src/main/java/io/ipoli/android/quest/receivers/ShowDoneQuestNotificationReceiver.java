package io.ipoli.android.quest.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class ShowDoneQuestNotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_DONE_QUEST_NOTIFICATION = "io.ipoli.android.intent.action.SHOW_DONE_QUEST_NOTIFICATION";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        QuestNotificationScheduler.stopTimer(questId, context);
        Quest q = questPersistenceService.findById(questId);

        NotificationCompat.Builder builder = createNotificationBuilder(context, q);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.QUEST_DONE_NOTIFICATION_ID, builder.build());
    }

    private NotificationCompat.Builder createNotificationBuilder(Context context, Quest q) {
        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(q.getName())
                .setContentText("Quest done! Ready for a break?")
                .setContentIntent(getPendingIntent(context, q.getId()))
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setWhen(q.getActualStartDateTime().getTime())
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true);
    }

    private PendingIntent getPendingIntent(Context context, String questId) {
        Intent i = new Intent(context, QuestActivity.class);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        i.setAction(QuestActivity.ACTION_QUEST_DONE);
        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, i, context);
    }

}
