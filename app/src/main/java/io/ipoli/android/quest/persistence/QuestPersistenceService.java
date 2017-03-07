package io.ipoli.android.quest.persistence;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestReminder;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService extends PersistenceService<Quest> {

    void listenForInboxQuests(OnDataChangedListener<List<Quest>> listener);

    void listenForPlannedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<SortedMap<LocalDate, List<Quest>>> listener);

    void findAllCompletedNonAllDayBetween(LocalDate startDate, LocalDate endDate, OnDataChangedListener<List<Quest>> listener);

    void findAllPlannedAndStarted(OnDataChangedListener<List<Quest>> listener);

    void findAllIncompleteToDosBefore(LocalDate date, OnDataChangedListener<List<Quest>> listener);

    void listenForAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllNonAllDayForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void listenForAllNonAllDayCompletedForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void listenForAllNonAllDayIncompleteForDate(LocalDate currentDate, OnDataChangedListener<List<Quest>> listener);

    void findAllUpcomingForRepeatingQuest(LocalDate startDate, String repeatingQuestId, OnDataChangedListener<List<Quest>> listener);

    void countAllCompletedWithPriorityForDate(int priority, LocalDate date, OnDataChangedListener<Long> listener);

    void findQuestRemindersAtStartTime(long startTime, OnDataChangedListener<List<QuestReminder>> listener);

    void findNextReminderTime(OnDataChangedListener<Long> listener);

    void listenForAllIncompleteOrMostImportantForDate(LocalDate now, OnDataChangedListener<List<Quest>> listener);

    void findIncompleteNotRepeatingNotForChallenge(String query, String challengeId, OnDataChangedListener<List<Quest>> listener);

    void listenForIncompleteNotRepeating(OnDataChangedListener<List<Quest>> listener);

    void listenForReminderChange(OnChangeListener onChangeListener);

    void deleteRemindersAtTime(long startTime);

    void populateNewQuestData(Quest quest, Map<String, Object> data);

    void populateDeleteQuestData(Quest quest, Map<String, Object> data);

    void save(List<Quest> quests);

    void populateDeleteQuestDataFromRepeatingQuest(Quest quest, Map<String, Object> data);
}