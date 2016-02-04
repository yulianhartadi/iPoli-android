package io.ipoli.android.quest.commands;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.ipoli.android.Constants;
import io.ipoli.android.quest.Quest;
import io.ipoli.android.quest.QuestNotificationScheduler;
import io.ipoli.android.quest.Status;
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
        quest.setStatus(Status.STARTED.name());
        quest.setActualStartDateTime(new Date());
        quest = questPersistenceService.save(quest);
        scheduleNextQuestReminder(context);
        stopOtherRunningQuests(quest);

        if (quest.getDuration() > 0) {
            long durationMillis = TimeUnit.MINUTES.toMillis(quest.getDuration());
            long showDoneAtMillis = quest.getActualStartDateTime().getTime() + durationMillis;
            QuestNotificationScheduler.scheduleDone(quest.getId(), showDoneAtMillis, context);
        }
    }


    private void stopOtherRunningQuests(Quest q) {
        List<Quest> quests = questPersistenceService.findAllPlannedForToday();
        for (Quest cq : quests) {
            if (!cq.getId().equals(q.getId()) && Status.valueOf(cq.getStatus()) == Status.STARTED) {
                cq.setStatus(Status.PLANNED.name());
                cq.setActualStartDateTime(null);
                questPersistenceService.save(cq);
            }
        }
    }

    private void scheduleNextQuestReminder(Context context) {
        context.sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}
