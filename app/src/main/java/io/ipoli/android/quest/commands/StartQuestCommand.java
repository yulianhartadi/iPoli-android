package io.ipoli.android.quest.commands;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.receivers.ScheduleQuestReminderReceiver;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StartQuestCommand {

    private Context context;
    private QuestPersistenceService questPersistenceService;
    private Quest quest;

    public StartQuestCommand(Context context, QuestPersistenceService questPersistenceService, Quest quest) {
        this.context = context;
        this.questPersistenceService = questPersistenceService;
        this.quest = quest;
    }

    public void execute() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.REMIND_START_QUEST_NOTIFICATION_ID);
        quest.setActualStart(DateUtils.nowUTC());
        questPersistenceService.save(quest).subscribe(q -> {
            scheduleNextQuestReminder(context);
            stopOtherRunningQuests(quest);

            if (quest.getDuration() > 0) {
                long durationMillis = TimeUnit.MINUTES.toMillis(quest.getDuration());
                long showDoneAtMillis = quest.getActualStart().getTime() + durationMillis;
                QuestNotificationScheduler.scheduleDone(quest.getId(), showDoneAtMillis, context);
            }
        });
    }


    private void stopOtherRunningQuests(Quest q) {

        questPersistenceService.findAllPlannedAndStartedToday().subscribe(quests -> {
            for (Quest cq : quests) {
                if (!cq.getId().equals(q.getId()) && Quest.isStarted(cq)) {
                    cq.setActualStart(null);
                    questPersistenceService.save(cq);
                }
            }
        });

    }

    private void scheduleNextQuestReminder(Context context) {
        context.sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}
