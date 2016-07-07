package io.ipoli.android.quest.schedulers;

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
            if (rq.isFlexible() && rq.getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.MONTHLY) {
                if (rq.getRecurrence().getRecurrenceType() == Recurrence.RecurrenceType.WEEKLY) {

                }
            } else {
                scheduleFor4WeeksAhead(rq, currentDate);
            }
        }
    }

    private void scheduleFor4WeeksAhead(RepeatingQuest rq, LocalDate currentDate) {

        LocalDate startOfWeek = currentDate.dayOfWeek().withMinimumValue();
        LocalDate endOfWeek = currentDate.dayOfWeek().withMaximumValue();

        List<Pair<LocalDate, LocalDate>> weekBounds = new ArrayList<>();
        weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        for (int i = 0; i < 3; i++) {
            startOfWeek = startOfWeek.plusDays(7);
            endOfWeek = endOfWeek.plusDays(7);
            weekBounds.add(new Pair<>(startOfWeek, endOfWeek));
        }

        for (Pair<LocalDate, LocalDate> weekPair : weekBounds) {
            saveQuestsInRange(rq, weekPair.first, weekPair.second);
        }
    }

    private void saveQuestsInRange(RepeatingQuest rq, LocalDate startOfWeek, LocalDate endOfWeek) {
        long createdQuestsCount = questPersistenceService.countAllForRepeatingQuest(rq, startOfWeek, endOfWeek);
        if (createdQuestsCount == 0) {
            List<Quest> questsToCreate = repeatingQuestScheduler.schedule(rq, DateUtils.toStartOfDayUTC(startOfWeek));
            questPersistenceService.saveSync(questsToCreate);
        }
    }
}
