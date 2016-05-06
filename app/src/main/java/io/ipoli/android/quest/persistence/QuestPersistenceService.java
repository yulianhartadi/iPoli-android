package io.ipoli.android.quest.persistence;

import org.joda.time.LocalDate;

import java.util.List;

import io.ipoli.android.app.persistence.PersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RecurrentQuest;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService extends PersistenceService<Quest> {
    Observable<Quest> findById(String id);

    Observable<List<Quest>> findAllUnplanned();

    Observable<List<Quest>> findPlannedBetween(LocalDate startDate, LocalDate endDate);

    Observable<List<Quest>> findAllPlannedAndStartedToday();

    Observable<List<Quest>> findAllIncompleteToDosBefore(LocalDate localDate);

    Observable<Quest> findPlannedQuestStartingAfter(LocalDate localDate);

    void delete(Quest quest);

    void deleteAllFromRecurrentQuest(String recurrentQuestId);

    long countCompletedQuests(RecurrentQuest recurrentQuest, LocalDate fromDate, LocalDate toDate);

    Observable<List<Quest>> findAllForDate(LocalDate currentDate);

    Observable<List<Quest>> findAllCompletedForDate(LocalDate currentDate);

    Observable<List<Quest>> findAllIncompleteForDate(LocalDate currentDate);
}
