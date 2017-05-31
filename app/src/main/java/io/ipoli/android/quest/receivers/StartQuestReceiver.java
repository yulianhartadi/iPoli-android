package io.ipoli.android.quest.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.events.StartUpgradeDialogRequestEvent;
import io.ipoli.android.player.UpgradeManager;
import io.ipoli.android.quest.commands.StartQuestCommand;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.store.Upgrade;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 10/13/16.
 */

public class StartQuestReceiver extends BroadcastReceiver {

    public static final String ACTION_START_QUEST = "io.ipoli.android.intent.action.START_QUEST";

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    UpgradeManager upgradeManager;

    @Inject
    Bus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);

        if(upgradeManager.isLocked(Upgrade.TIMER)) {
            eventBus.post(new StartUpgradeDialogRequestEvent(Upgrade.TIMER));
//            Toast.makeText(context, "Times is locked, go to Store to buy it", Toast.LENGTH_LONG).show();
            return;
        }

        PendingResult result = goAsync();
        String questId = intent.getStringExtra(Constants.QUEST_ID_EXTRA_KEY);
        questPersistenceService.findById(questId, quest -> {
            QuestNotificationScheduler.scheduleUpdateTimer(questId, context);
            new StartQuestCommand(context, quest, questPersistenceService).execute();
            Toast.makeText(context, R.string.quest_started, Toast.LENGTH_SHORT).show();
            result.finish();
        });
    }
}
