package io.ipoli.android.app.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/11/17.
 */

public interface CalendarPersistenceService {

    void saveSync(Player player, List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests);

    void updateSync(Player player, List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests, Set<Long> calendarsToRemove, Map<Long, Category> calendarsToUpdate);
}
