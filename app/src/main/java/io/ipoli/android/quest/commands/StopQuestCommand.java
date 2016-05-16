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
    private String questId;
    private QuestPersistenceService questPersistenceService;

    public StopQuestCommand(Context context, String questId, QuestPersistenceService questPersistenceService) {
        this.context = context;
        this.questId = questId;
        this.questPersistenceService = questPersistenceService;
    }

    public Observable<Quest> execute() {
        return questPersistenceService.findById(questId).flatMap(quest -> {
            quest.setActualStart(null);
            return questPersistenceService.save(quest).flatMap(q -> {
                QuestNotificationScheduler.stopAll(q.getId(), context);
                return Observable.just(q);
            });
        });
    }
}
