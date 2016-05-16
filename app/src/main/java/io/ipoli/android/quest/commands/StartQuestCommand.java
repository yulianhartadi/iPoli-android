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
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/2/16.
 */
public class StartQuestCommand {

    private Context context;
    private final String questId;
    private QuestPersistenceService questPersistenceService;

    public StartQuestCommand(Context context, String questId, QuestPersistenceService questPersistenceService) {
        this.context = context;
        this.questId = questId;
        this.questPersistenceService = questPersistenceService;
    }

    public Observable<Quest> execute() {
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.cancel(Constants.REMIND_START_QUEST_NOTIFICATION_ID);
        return questPersistenceService.findById(questId).flatMap(quest -> {
            quest.setActualStart(DateUtils.nowUTC());
            return questPersistenceService.save(quest).flatMap(q -> {
                scheduleNextQuestReminder(context);
                stopOtherRunningQuests(quest);

                if (quest.getDuration() > 0) {
                    long durationMillis = TimeUnit.MINUTES.toMillis(quest.getDuration());
                    long showDoneAtMillis = quest.getActualStart().getTime() + durationMillis;
                    QuestNotificationScheduler.scheduleDone(quest.getId(), showDoneAtMillis, context);
                }
                return Observable.just(q);
            });
        });
    }

    private void stopOtherRunningQuests(Quest q) {

        questPersistenceService.findAllPlannedAndStartedToday().subscribe(quests -> {
            for (Quest cq : quests) {
                if (!cq.getId().equals(q.getId()) && Quest.isStarted(cq)) {
                    cq.setActualStart(null);
                    questPersistenceService.save(cq).subscribe();
                }
            }
        });
    }

    private void scheduleNextQuestReminder(Context context) {
        context.sendBroadcast(new Intent(ScheduleQuestReminderReceiver.ACTION_SCHEDULE_REMINDER));
    }
}
