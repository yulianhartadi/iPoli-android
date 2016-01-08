package io.ipoli.android.quest;

import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService {
    Quest save(Quest quest);

    List<Quest> findAll();
}
