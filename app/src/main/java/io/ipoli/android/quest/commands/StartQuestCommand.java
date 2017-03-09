package io.ipoli.android.quest.commands;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.schedulers.QuestNotificationScheduler;
import io.ipoli.android.reminder.data.Reminder;

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

    public Quest execute() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        for (Reminder r : quest.getReminders()) {
            notificationManagerCompat.cancel(r.getNotificationNum());
        }

        quest.setActualStartDate(DateUtils.nowUTC());
        questPersistenceService.save(quest);
        stopOtherRunningQuests(quest);

        if (quest.getDuration() > 0) {
            long durationMillis = TimeUnit.MINUTES.toMillis(quest.getDuration());
            long showDoneAtMillis = quest.getActualStartDate().getTime() + durationMillis;
            QuestNotificationScheduler.scheduleDone(quest.getId(), showDoneAtMillis, context);
        }
        return quest;
    }

    private void stopOtherRunningQuests(Quest q) {
        questPersistenceService.findAllPlannedAndStarted(quests -> {
            for (Quest cq : quests) {
                if (!cq.getId().equals(q.getId()) && Quest.isStarted(cq)) {
                    cq.setActualStartDate(null);
                }
            }
            questPersistenceService.save(quests);
        });
    }
}
