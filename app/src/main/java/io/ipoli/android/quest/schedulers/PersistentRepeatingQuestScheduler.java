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
                    scheduleForMonth(rq, currentDate);
                } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
                    for (Pair<LocalDate, LocalDate> weekPair : getBoundsFor4WeeksAhead(currentDate)) {
                        saveQuestsInRange(rq, weekPair.first, weekPair.second, currentDate);
                    }
                }
            } else {
                scheduleFor4WeeksAhead(rq, currentDate);
            }
        }

    }

    private void scheduleForMonth(RepeatingQuest repeatingQuest, LocalDate currentDate) {
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
