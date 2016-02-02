package io.ipoli.android.quest.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestActivity;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/1/16.
 */
public class StartQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_START_QUEST = "io.ipoli.android.intent.action.START_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    Bus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        Quest q = getQuest(intent);
        new StartQuestCommand(context, questPersistenceService, q).execute();
        startQuestActivity(context, q.getId());
    }

    private void startQuestActivity(Context context, String questId) {
        Intent i = new Intent(context, QuestActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Constants.QUEST_ID_EXTRA_KEY, questId);
        context.startActivity(i);
    }

    private Quest getQuest(Intent intent) {
        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        return questPersistenceService.findById(questId);
    }


}
