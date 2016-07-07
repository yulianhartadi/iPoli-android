package io.ipoli.android.quest.schedulers;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.List;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/16.
 */
public class PersistentRepeatingQuestScheduler {

    private final RepeatingQuestScheduler repeatingQuestScheduler;
    private final QuestPersistenceService questPersistenceService;

    public PersistentRepeatingQuestScheduler(RepeatingQuestScheduler repeatingQuestScheduler, QuestPersistenceService questPersistenceService) {
        this.repeatingQuestScheduler = repeatingQuestScheduler;
        this.questPersistenceService = questPersistenceService;
    }

    public void schedule(List<RepeatingQuest> repeatingQuests, java.util.Date startDate) {
        LocalDate currentDate = new LocalDate(startDate, DateTimeZone.UTC);
        LocalDate startOfWeek = currentDate.dayOfWeek().withMinimumValue();
        LocalDate endOfWeek = currentDate.dayOfWeek().withMaximumValue();
        LocalDate startOfNextWeek = startOfWeek.plusDays(7);
        LocalDate endOfNextWeek = endOfWeek.plusDays(7);
        for (RepeatingQuest rq : repeatingQuests) {
            if (rq.isFlexible()) {
                if (rq.getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.WEEKLY) {

                }
            } else {
                scheduleFixedFor4WeeksAhead(startOfWeek, endOfWeek, startOfNextWeek, endOfNextWeek, rq);
            }
        }
    }

    private void scheduleFixedFor4WeeksAhead(LocalDate startOfWeek, LocalDate endOfWeek, LocalDate startOfNextWeek, LocalDate endOfNextWeek, RepeatingQuest rq) {
        saveQuestsInRange(rq, startOfWeek, endOfWeek);
        saveQuestsInRange(rq, startOfNextWeek, endOfNextWeek);
    }

    private void saveQuestsInRange(RepeatingQuest rq, LocalDate startOfWeek, LocalDate endOfWeek) {
        long createdQuestsCount = questPersistenceService.countAllForRepeatingQuest(rq, startOfWeek, endOfWeek);
        if (createdQuestsCount == 0) {
            List<Quest> questsToCreate = repeatingQuestScheduler.schedule(rq, DateUtils.toStartOfDayUTC(startOfWeek));
            questPersistenceService.saveSync(questsToCreate);
        }
    }
}
