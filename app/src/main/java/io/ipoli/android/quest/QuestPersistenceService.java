package io.ipoli.android.quest;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService {
    Quest save(Quest quest);

    List<Quest> saveAll(List<Quest> quests);

    List<Quest> findAllUncompleted();

    List<Quest> findAllForToday();
}
