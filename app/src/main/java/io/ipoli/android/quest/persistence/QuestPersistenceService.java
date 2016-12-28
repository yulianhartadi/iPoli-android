package io.ipoli.android.quest.persistence;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService extends PersistenceService<Quest> {

    void listenForInboxQuests(OnDataChangedListener<List<Quest>> listener);

    void listenForPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener);

    void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener);

    void findAllPlannedAndStartedToday(OnDataChangedListener<List<Quest>> listener);

    void findAllIncompleteToDosBefore(LocalDate date, OnDataChangedListener<List<Quest>> listener);

    void listenForAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void listenForAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void listenForAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllNotCompletedForRepeatingQuest(String repeatingQuestId, OnDataChangedListener<List<Quest>> listener);

    void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener);

    void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener);

    void findNextQuestIdsToRemind(OnDataChangedListener<ReminderStart> listener);

    void findAllIncompleteOrMostImportantForDate(LocalDate now, OnDataChangedListener<List<Quest>> listener);

    void findIncompleteNotRepeatingNotForChallenge(String query, String challengeId, OnDataChangedListener<List<Quest>> listener);

    void listenForReminderChange(OnChangeListener<Void> onChangeListener);

    void deleteRemindersAtTime(long startTime, OnOperationCompletedListener listener);

    void saveNewQuest(Quest quest);

    void deleteNewQuest(Quest quest);

    void updateNewQuest(Quest quest);

    void populateNewQuestData(Quest quest, Map<String, Object> data);

    void populateDeleteQuestData(Quest quest, Map<String, Object> data);

    void saveNewQuests(List<Quest> quests);

    void updateNewQuests(List<Quest> quests);
}