package io.ipoli.android.quest.persistence;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService extends PersistenceService<Quest> {

    void listenForUnplanned(OnDataChangedListener<List<Quest>> listener);

    void listenForPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener);

    void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener);

    void findAllPlannedAndStartedToday(OnDataChangedListener<List<Quest>> listener);

    void findAllIncompleteToDosBefore(LocalDate date, OnDataChangedListener<List<Quest>> listener);

    void findCompletedWithStartTimeForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener);

    long countCompleted(RepeatingQuest repeatingQuest, LocalDate fromDate, LocalDate toDate);

    long countCompleted(RepeatingQuest repeatingQuest);

    void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    List<Quest> findAllForRepeatingQuest(RepeatingQuest repeatingQuest);

    long countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate);

    List<Quest> findAllNonAllDayIncompleteForDateSync(LocalDate currentDate);

    Quest findByExternalSourceMappingId(String source, String sourceId);

    List<Quest> findAllUpcomingForRepeatingQuest(LocalDate startDate, RepeatingQuest repeatingQuest);

    long countAllCompletedWithPriorityForDate(int priority, LocalDate date);

    List<Quest> findAllForChallenge(Challenge challenge);

    Quest findByReminderId(String reminderId);

    void findAllIncompleteOrMostImportantForDate(LocalDate now, OnDataChangedListener<List<Quest>> listener);

    Date findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest);

    void findNextUncompletedQuestEndDate(Challenge challenge, OnDataChangedListener<Date> listener);

    void findIncompleteNotRepeatingForChallenge(Challenge challenge, OnDataChangedListener<List<Quest>> listener);

    void findIncompleteNotRepeatingNotForChallenge(String query, Challenge challenge, OnDataChangedListener<List<Quest>> listener);

    void findAllCompleted(Challenge challenge, OnDataChangedListener<List<Quest>> listener);

    void countCompletedByWeek(Challenge challenge, int weeks, OnDataChangedListener<List<Long>> listener);

    void countCompleted(Challenge challenge, OnDataChangedListener<Long> listener);

    void countNotRepeating(Challenge challenge, OnDataChangedListener<Long> listener);

    void countNotDeleted(Challenge challenge, OnDataChangedListener<Long> listener);
}