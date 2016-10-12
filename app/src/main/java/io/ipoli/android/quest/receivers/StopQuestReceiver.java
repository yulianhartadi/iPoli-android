package io.ipoli.android.quest.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.quest.commands.StopQuestCommand;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 10/13/16.
 */

public class StopQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_STOP_QUEST = "io.ipoli.android.intent.action.STOP_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        BroadcastReceiver.PendingResult result = goAsync();
        App.getAppComponent(context).inject(this);

        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, quest -> {
            new StopQuestCommand(context, quest, questPersistenceService).execute();
            Toast.makeText(context, R.string.quest_stopped, Toast.LENGTH_SHORT).show();
            result.finish();
        });
    }
}
