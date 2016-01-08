package io.ipoli.android.quest;

import java.util.Date;
import java.util.List;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public interface QuestPersistenceService {
    Quest save(Quest quest);

    List<Quest> findAllUncompleted();

    void update(Quest quest, String status, Date due);
}
