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

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.navigation.ActivityIntentFactory;
import io.ipoli.android.app.utils.IntentUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.activities.QuestActivity;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/17/16.
 */
public class RemindStartQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_REMIND_START_QUEST = "io.ipoli.android.intent.action.REMIND_START_QUEST";

    @Inject
    Bus eventBus;

    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        Realm realm = Realm.getDefaultInstance();
        questPersistenceService = new RealmQuestPersistenceService(eventBus, realm);
        Quest q = questPersistenceService.findById(questId);
        if (q == null) {
            return;
        }
        showNotification(context, questId, q);
    }

    private void showNotification(Context context, String questId, Quest q) {
        Intent remindStartQuestIntent = new Intent(context, QuestActivity.class);
        remindStartQuestIntent.setAction(ACTION_REMIND_START_QUEST);
        remindStartQuestIntent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        String name = q.getName();

        PendingIntent pendingNotificationIntent = ActivityIntentFactory.createWithParentStack(QuestActivity.class, remindStartQuestIntent, context);

        Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        PendingIntent startQuestPI = getStartPendingIntent(q.getId(), context);
        PendingIntent snoozeQuestPI = getSnoozePendingIntent(q.getId(), context);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                .setContentTitle(name)
                .setContentText("Ready to start?")
                .setContentIntent(pendingNotificationIntent)
                .setShowWhen(false)
                .setContentInfo(Time.of(q.getStartMinute()).toString())
                .setSmallIcon(R.drawable.ic_notification_small)
                .setLargeIcon(largeIcon)
                .setOnlyAlertOnce(true)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_snooze_black_24dp, "SNOOZE", snoozeQuestPI)
                .addAction(R.drawable.ic_play_arrow_black_24dp, "START", startQuestPI)
                .setDefaults(NotificationCompat.DEFAULT_VIBRATE | NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_LIGHTS)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(Constants.REMIND_START_QUEST_NOTIFICATION_ID, builder.build());
    }


    private PendingIntent getStartPendingIntent(String questId, Context context) {
        Intent intent = new Intent(context, QuestActivity.class);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        intent.setAction(QuestActivity.ACTION_START_QUEST);

        return ActivityIntentFactory.createWithParentStack(QuestActivity.class, intent, context);
    }

    private PendingIntent getSnoozePendingIntent(String questId, Context context) {
        Intent intent = new Intent(SnoozeQuestReceiver.ACTION_SNOOZE_QUEST);
        intent.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        return IntentUtils.getBroadcastPendingIntent(context, intent);
    }
}
