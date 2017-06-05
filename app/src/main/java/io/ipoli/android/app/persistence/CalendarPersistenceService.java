package io.ipoli.android.app.persistence;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.player.Player;
import io.ipoli.android.quest.data.AndroidCalendarMapping;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/11/17.
 */

public interface CalendarPersistenceService {

    void updateSync(Player player, List<Quest> quests, Set<Long> calendarsToRemove, Map<Long, Category> calendarsToUpdate);

    void updateSync(Set<Quest> questsToSave, Set<Quest> questsToDelete, List<AndroidCalendarMapping> mappingsToDelete);

    void deleteAllCalendarsSync(Player player);
}
