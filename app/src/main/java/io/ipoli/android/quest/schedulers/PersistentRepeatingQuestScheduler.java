package io.ipoli.android.quest.schedulers;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/7/16.
 */
public class PersistentRepeatingQuestScheduler {

    private final RepeatingQuestScheduler repeatingQuestScheduler;

    private final QuestPersistenceService questPersistenceService;

    private final RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    public PersistentRepeatingQuestScheduler(RepeatingQuestScheduler repeatingQuestScheduler, QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        this.repeatingQuestScheduler = repeatingQuestScheduler;
        this.questPersistenceService = questPersistenceService;
        this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
    }

    public List<Quest> schedule(RepeatingQuest repeatingQuest, java.util.Date startDate) {
        LocalDate currentDate = new LocalDate(startDate, DateTimeZone.UTC);
        List<Quest> quests = new ArrayList<>();
        if (repeatingQuest.isFlexible()) {
            Recurrence.RecurrenceType recurrenceType = repeatingQuest.getRecurrence().getRecurrenceType();
            if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
                quests.addAll(scheduleFlexibleForMonth(repeatingQuest, currentDate));
            } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
                quests.addAll(scheduleFlexibleFor4WeeksAhead(currentDate, repeatingQuest));
            }
        } else {
            quests.addAll(scheduleFor4WeeksAhead(repeatingQuest, currentDate));
        }
        return quests;
    }

    public void schedule(List<RepeatingQuest> repeatingQuests, java.util.Date startDate) {
        LocalDate currentDate = new LocalDate(startDate, DateTimeZone.UTC);
        List<Quest> quests = new ArrayList<>();
        for (RepeatingQuest rq : repeatingQuests) {
            if (rq.isFlexible()) {
                Recurrence.RecurrenceType recurrenceType = rq.getRecurrence().getRecurrenceType();
                if (recurrenceType == Recurrence.RecurrenceType.MONTHLY) {
                    quests.addAll(scheduleFlexibleForMonth(rq, currentDate));
                } else if (recurrenceType == Recurrence.RecurrenceType.WEEKLY) {
                    quests.addAll(scheduleFlexibleFor4WeeksAhead(currentDate, rq));
                }
            } else {
                quests.addAll(scheduleFor4WeeksAhead(rq, currentDate));
            }
        }
        questPersistenceService.save(quests);
        repeatingQuestPersistenceService.save(repeatingQuests);
    }

    private List<Quest> scheduleFlexibleFor4WeeksAhead(LocalDate currentDate, RepeatingQuest rq) {
        List<Pair<LocalDate, LocalDate>> bounds = getBoundsFor4WeeksAhead(currentDate);
        List<Quest> quests = new ArrayList<>();
        for (int i = 0; i < bounds.size(); i++) {
            Pair<LocalDate, LocalDate> weekPair = bounds.get(i);
            // Start date is relevant only for the current week. Next week starts (naturally) at the start of the next week.
            LocalDate startDate = i == 0 ? currentDate : weekPair.first;
            quests.addAll(saveQuestsInRange(rq, startDate, weekPair.second));
        }
        return quests;
    }

    private List<Quest> scheduleFlexibleForMonth(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        LocalDate endOfMonth = currentDate.dayOfMonth().withMaximumValue();
        return saveQuestsInRange(repeatingQuest, currentDate, endOfMonth);
    }

    private List<Quest> scheduleFor4WeeksAhead(RepeatingQuest repeatingQuest, LocalDate currentDate) {
        List<Quest> quests = new ArrayList<>();
        for (Pair<LocalDate, LocalDate> weekPair : getBoundsFor4WeeksAhead(currentDate)) {
            quests.addAll(saveQuestsInRange(repeatingQuest, weekPair.first, weekPair.second));
        }
        return quests;
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

    private List<Quest> saveQuestsInRange(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endOfPeriodDate) {
        Date periodEnd = DateUtils.toStartOfDayUTC(endOfPeriodDate);
        if (!repeatingQuest.shouldBeScheduledForPeriod(periodEnd)) {
            return new ArrayList<>();
        }
        List<Quest> questsToCreate = repeatingQuestScheduler.schedule(repeatingQuest, DateUtils.toStartOfDayUTC(startDate));
        repeatingQuest.addScheduledPeriodEndDate(periodEnd);
        return questsToCreate;
    }
}