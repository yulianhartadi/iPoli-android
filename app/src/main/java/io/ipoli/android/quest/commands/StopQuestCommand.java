package io.ipoli.android.quest.commands;

import android.content.Context;

import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.Status;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StopQuestCommand {

    private Quest quest;
    private QuestPersistenceService questPersistenceService;
    private Context context;

    public StopQuestCommand(Quest quest, QuestPersistenceService questPersistenceService, Context context) {
        this.quest = quest;
        this.questPersistenceService = questPersistenceService;
        this.context = context;
    }

    public void execute() {
        quest.setStatus(Status.PLANNED.name());
        quest.setActualStartDateTime(null);
        quest = questPersistenceService.save(quest);
        QuestNotificationScheduler.stopAll(quest.getId(), context);
    }
}
