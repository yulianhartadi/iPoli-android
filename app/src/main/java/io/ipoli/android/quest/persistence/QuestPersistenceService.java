package io.ipoli.android.quest.persistence;

import java.util.Date;
import java.util.List;

import io.ipoli.android.quest.data.Quest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService {
    Quest findById(String id);

    List<Quest> findAllCompleted();

    List<Quest> findAllUnplanned();

    List<Quest> findAllPlanned();

    List<Quest> findAllPlannedAndStartedToday();

    List<Quest> findAllUncompleted();

    List<Quest> findAllCompletedToday();

    List<Quest> findAllWhoNeedSyncWithRemote();

    Quest findPlannedQuestStartingAfter(Date date);

    Quest save(Quest quest);

    List<Quest> saveAll(List<Quest> quests);

    void delete(Quest quest);
}
