package io.ipoli.android.quest.persistence;

import org.joda.time.LocalDate;

import java.util.Date;
import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
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

    void countCompletedForRepeatingQuest(String repeatingQuestId, LocalDate fromDate, LocalDate toDate, OnDataChangedListener<Long> listener);

    void countCompletedForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<Long> listener);

    void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener);

    long countAllForRepeatingQuest(RepeatingQuest repeatingQuest, LocalDate startDate, LocalDate endDate);

    List<Quest> findAllNonAllDayIncompleteForDateSync(LocalDate currentDate);

    void findByExternalSourceMappingId(String source, String sourceId, OnDataChangedListener<Quest> listener);

    void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener);

    void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener);

    void findAllForChallenge(String challengeId, OnDataChangedListener<List<Quest>> listener);

    Quest findByReminderId(String reminderId);

    void findAllIncompleteOrMostImportantForDate(LocalDate now, OnDataChangedListener<List<Quest>> listener);

    Date findNextUncompletedQuestEndDate(RepeatingQuest repeatingQuest);

    void findNextUncompletedQuestEndDate(String challengeId, OnDataChangedListener<Date> listener);

    void findIncompleteNotRepeatingForChallenge(String challengeId, OnDataChangedListener<List<Quest>> listener);

    void findIncompleteNotRepeatingNotForChallenge(String query, String challengeId, OnDataChangedListener<List<Quest>> listener);

    void findAllCompleted(String challengeId, OnDataChangedListener<List<Quest>> listener);

    void countCompletedByWeek(String challengeId, int weeks, OnDataChangedListener<List<Long>> listener);

    void countCompletedForChallenge(String challengeId, OnDataChangedListener<Long> listener);

    void countNotRepeating(String challengeId, OnDataChangedListener<Long> listener);

    void countNotDeleted(String challengeId, OnDataChangedListener<Long> listener);
}