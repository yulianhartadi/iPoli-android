package io.ipoli.android.quest.commands;

import android.content.Context;

import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StopQuestCommand {

    private Context context;
    private Quest quest;
    private QuestPersistenceService questPersistenceService;

    public StopQuestCommand(Context context, Quest quest, QuestPersistenceService questPersistenceService) {
        this.context = context;
        this.quest = quest;
        this.questPersistenceService = questPersistenceService;
    }

    public Observable<Quest> execute() {
        quest.setActualStart(null);
        return questPersistenceService.save(quest).flatMap(q -> {
            QuestNotificationScheduler.stopAll(q.getId(), context);
            return Observable.just(q);
        });
    }
}
