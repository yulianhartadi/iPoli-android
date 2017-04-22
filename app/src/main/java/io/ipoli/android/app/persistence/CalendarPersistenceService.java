package io.ipoli.android.app.persistence;

import android.util.Pair;

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

    void updateAsync(List<Quest> quests, Map<RepeatingQuest, Pair<List<Quest>, List<Quest>>> repeatingQuests);

    void deleteAllCalendarsSync(Player player);
}
