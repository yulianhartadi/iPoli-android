package io.ipoli.android.quest.persistence;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.reminders.data.Reminder;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SubQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService extends PersistenceService<Quest> {

    void listenForUnplanned(OnDatabaseChangedListener<List<Quest>> listener);

    void findPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDatabaseChangedListener<List<Quest>> listener);

    List<Quest> findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate);

    List<Quest> findAllPlannedAndStartedToday();

    List<Quest> findAllIncompleteToDosBefore(LocalDate localDate);

    List<Quest> findAllCompletedWithStartTime(RepeatingQuest repeatingQuest);

    long countCompleted(RepeatingQuest repeatingQuest, LocalDate fromDate, LocalDate toDate);

    long countCompleted(RepeatingQuest repeatingQuest);

    void findAllNonAllDayForDate(LocalDate currentDate, OnDatabaseChangedListener<List<Quest>> listener);

    void findAllNonAllDayCompletedForDate(LocalDate currentDate, OnDatabaseChangedListener<List<Quest>> listener);

    void findAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDatabaseChangedListener<List<Quest>> listener);

    List<Quest> findAllForRepeatingQuest(RepeatingQuest repeatingQuest);

    long countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate);

    List<Quest> findAllNonAllDayIncompleteForDateSync(LocalDate currentDate);

    Quest findByExternalSourceMappingId(String source, String sourceId);

    List<Quest> findAllUpcomingForRepeatingQuest(LocalDate startDate, RepeatingQuest repeatingQuest);

    long countAllCompletedWithPriorityForDate(int priority, LocalDate date);

    List<Quest> findAllForChallenge(Challenge challenge);

    Quest findByReminderId(String reminderId);

    void findAllIncompleteOrMostImportantForDate(LocalDate now, OnDatabaseChangedListener<List<Quest>> listener);

    void saveReminders(Quest quest, List<Reminder> reminders);

    void saveSubQuests(Quest quest, List<SubQuest> subQuests);

    Date findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest);

    Date findNextUncompletedQuestEndDate(Challenge challenge);

    void findIncompleteNotRepeatingForChallenge(Challenge challenge, OnDatabaseChangedListener<List<Quest>> listener);

    List<Quest> findIncompleteNotRepeatingNotForChallenge(String query, Challenge challenge);

    List<Quest> findAllCompleted(Challenge challenge);

    long countCompleted(Challenge challenge, LocalDate start, LocalDate end);

    long countCompleted(Challenge challenge);

    long countNotRepeating(Challenge challenge);

    long countNotDeleted(Challenge challenge);
}