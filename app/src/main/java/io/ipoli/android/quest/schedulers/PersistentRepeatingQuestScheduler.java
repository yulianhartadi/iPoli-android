package io.ipoli.android.quest.schedulers;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.ArrayList;
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
        for (RepeatingQuest rq : repeatingQuests) {
            if (rq.isFlexible()) {
                Recurrence.RecurrenceType recurrenceType = rq.getRecurrence().getRecurrenceType();
                if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
                    scheduleFlexibleForMonth(rq, currentDate);
                } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
                    scheduleFlexibleFor4WeeksAhead(currentDate, rq);
                }
            } else {
                scheduleFor4WeeksAhead(rq, currentDate);
            }
        }
    }

    private void scheduleFlexibleFor4WeeksAhead(LocalDate currentDate, RepeatingQuest rq) {
        List<Pair<LocalDate, LocalDate>> bounds = getBoundsFor4WeeksAhead(currentDate);
        for (int i = 0; i < bounds.size(); i++) {
            Pair<LocalDate, LocalDate> weekPair = bounds.get(i);
            // Start date is relevant only for the current week. Next week starts (naturally) at next first date of week.
            LocalDate startDate = i == 0 ? currentDate : weekPair.first;
            saveQuestsInRange(rq, weekPair.first, weekPair.second, startDate);
        }
    }

    private void scheduleFlexibleForMonth(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        LocalDate startOfMonth = currentDate.dayOfMonth().withMinimumValue();
        LocalDate endOfMonth = currentDate.dayOfMonth().withMaximumValue();
        saveQuestsInRange(repeatingQuest, startOfMonth, endOfMonth, currentDate);
    }

    private void scheduleFor4WeeksAhead(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        for (Pair<LocalDate, LocalDate> weekPair : getBoundsFor4WeeksAhead(currentDate)) {
            saveQuestsInRange(repeatingQuest, weekPair.first, weekPair.second);
        }
    }

    @NonNull
    private List<Pair<LocalDate, LocalDate>> getBoundsFor4WeeksAhead(LocalDate currentDate) {
        LocalDate startOfWeek = currentDate.dayOfWeek().withMinimumValue();
        LocalDate endOfWeek = currentDate.dayOfWeek().withMaximumValue();

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        for (int i = 0; i < 3; i++) {
            startOfWeek = startOfWeek.plusDays(7);
            endOfWeek = endOfWeek.plusDays(7);
            weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        }
        return weekBounds;
    }

    private void saveQuestsInRange(RepeatingQuest repeatingQuest, LocalDate startOfPeriodDate, LocalDate endOfPeriodDate, LocalDate startDate) {
        long createdQuestsCount = questPersistenceService.countAllForRepeatingQuest(repeatingQuest, startOfPeriodDate, endOfPeriodDate);
        if (createdQuestsCount == 0) {
            List<Quest> questsToCreate = repeatingQuestScheduler.schedule(repeatingQuest, DateUtils.toStartOfDayUTC(startDate));
            questPersistenceService.saveSync(questsToCreate);
        }
    }

    private void saveQuestsInRange(RepeatingQuest repeatingQuest, LocalDate startOfPeriodDate, LocalDate endOfPeriodDate) {
        saveQuestsInRange(repeatingQuest, startOfPeriodDate, endOfPeriodDate, startOfPeriodDate);
    }
}
