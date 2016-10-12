package io.ipoli.android.quest.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.quest.events.CompleteQuestRequestEvent;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 10/13/16.
 */
public class CompleteQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_COMPLETE_QUEST = "io.ipoli.android.intent.action.COMPLETE_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    Bus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        PendingResult result = goAsync();

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, quest -> {
            eventBus.post(new CompleteQuestRequestEvent(quest, EventSource.NOTIFICATION));
            result.finish();
        });
    }
}
