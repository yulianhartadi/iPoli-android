package io.ipoli.android.quest.persistence;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService extends PersistenceService<Quest> {

    void findAllUnplanned(OnDatabaseChangedListener<Quest> listener);

    void findPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDatabaseChangedListener<Quest> listener);

    List<Quest> findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate);

    List<Quest> findAllPlannedAndStartedToday();

    List<Quest> findAllIncompleteToDosBefore(LocalDate localDate);

    long countCompletedQuests(RepeatingQuest repeatingQuest, LocalDate fromDate, LocalDate toDate);

    void findAllNonAllDayForDate(LocalDate currentDate, OnDatabaseChangedListener<Quest> listener);

    void findAllNonAllDayCompletedForDate(LocalDate currentDate, OnDatabaseChangedListener<Quest> listener);

    void findAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDatabaseChangedListener<Quest> listener);

    List<Quest> findAllForRepeatingQuest(RepeatingQuest repeatingQuest);

    long countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate);

    long countAllScheduledForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate);

    List<Quest> findAllNonAllDayIncompleteForDateSync(LocalDate currentDate);

    Quest findByExternalSourceMappingId(String source, String sourceId);

    List<Quest> findAllUpcomingForRepeatingQuest(LocalDate startDate, RepeatingQuest repeatingQuest);

    long countAllCompletedWithPriorityForDate(int priority, LocalDate date);

    List<Quest> findAllForChallenge(Challenge challenge);

    Quest findByReminderId(String reminderId);

    void findAllIncompleteOrMostImportantForDate(LocalDate now, OnDatabaseChangedListener<Quest> listener);

    void saveReminders(Quest quest, List<Reminder> reminders);

    void saveReminders(Quest quest, List<Reminder> reminders,  boolean markUpdated);

    Date findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest);
}