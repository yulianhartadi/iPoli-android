package io.ipoli.android.quest.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.events.ScheduleNextQuestReminderEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/21/16.
 */
public class UpdateQuestIntentService extends IntentService {

    public static final String ACTION_START_QUEST = "io.ipoli.android.action.START_QUEST";
    public static final String ACTION_SNOOZE_QUEST = "io.ipoli.android.action.SNOOZE_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    Bus eventBus;

    public UpdateQuestIntentService() {
        super("iPoli-UpdateQuestIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            Log.d("iPoli", getClass().getSimpleName() + " was started without action");
            return;
        }
        App.getAppComponent(this).inject(this);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.cancel(Constants.REMIND_START_QUEST_NOTIFICATION_ID);
        if (action.equals(ACTION_START_QUEST)) {
            String questId = intent.getStringExtra("id");
            Quest q = questPersistenceService.findById(questId);
            q.setStatus(Quest.Status.STARTED.name());
            questPersistenceService.save(q);
            showQuestStartedNotification(q);
            eventBus.post(new ScheduleNextQuestReminderEvent());
        } else if (action.equals(ACTION_SNOOZE_QUEST)) {

        }
    }

    private void showQuestStartedNotification(Quest q) {

    }
}
