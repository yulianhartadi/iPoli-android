package io.ipoli.android.quest.commands;

import android.content.Context;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StartQuestCommand {

    private final Context context;
    private final Quest quest;
    private final QuestPersistenceService questPersistenceService;

    public StartQuestCommand(Context context, Quest quest, QuestPersistenceService questPersistenceService) {
        this.context = context;
        this.quest = quest;
        this.questPersistenceService = questPersistenceService;
    }

    public Observable<Quest> execute() {
        quest.setActualStart(DateUtils.nowUTC());
        return questPersistenceService.save(quest).flatMap(q -> {
            stopOtherRunningQuests(quest);

            if (quest.getDuration() > 0) {
                long durationMillis = TimeUnit.MINUTES.toMillis(quest.getDuration());
                long showDoneAtMillis = quest.getActualStart().getTime() + durationMillis;
                QuestNotificationScheduler.scheduleDone(quest.getId(), showDoneAtMillis, context);
            }
            return Observable.just(q);
        });
    }

    private void stopOtherRunningQuests(Quest q) {
        List<Quest> quests = questPersistenceService.findAllPlannedAndStartedToday();
        for (Quest cq : quests) {
            if (!cq.getId().equals(q.getId()) && Quest.isStarted(cq)) {
                cq.setActualStart(null);
                questPersistenceService.save(cq).subscribe();
            }
        }
    }
}
