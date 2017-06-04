package io.ipoli.android.app.persistence;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.AndroidCalendarMapping;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/10/17.
 */

public class AndroidCalendarPersistenceService implements CalendarPersistenceService {
    private final Database database;
    private final PlayerPersistenceService playerPersistenceService;
    private final QuestPersistenceService questPersistenceService;
    private final Bus eventBus;

    public AndroidCalendarPersistenceService(Database database, PlayerPersistenceService playerPersistenceService, QuestPersistenceService questPersistenceService, Bus eventBus) {
        this.database = database;
        this.playerPersistenceService = playerPersistenceService;
        this.questPersistenceService = questPersistenceService;
        this.eventBus = eventBus;
    }

    @Override
    public void updateSync(Player player, List<Quest> quests, Set<Long> calendarsToRemove, Map<Long, Category> calendarsToUpdate) {
        doUpdateSync(player, quests, new ArrayList<>(), calendarsToRemove, calendarsToUpdate);
    }

    @Override
    public void updateSync(List<Quest> questsToSave, List<AndroidCalendarMapping> questsToDelete) {
        doUpdateSync(null, questsToSave, questsToDelete, new HashSet<>(), new HashMap<>());
    }

    private void doUpdateSync(Player player, List<Quest> quests, List<AndroidCalendarMapping> mappingsToDelete, Set<Long> calendarsToRemove, Map<Long, Category> calendarsToUpdate) {
        database.runInTransaction(() -> {
            try {
                deleteCalendars(calendarsToRemove);
            } catch (CouchbaseLiteException e) {
                postError(e);
                return false;
            }

            questPersistenceService.save(quests);
            if (player != null) {
                savePlayer(player);
            }

            for (Long calendarId : calendarsToUpdate.keySet()) {
                Category category = calendarsToUpdate.get(calendarId);
                List<Quest> questsToUpdate = questPersistenceService.findFromAndroidCalendar(calendarId);
                for (Quest q : questsToUpdate) {
                    q.setCategoryType(category);
                    questPersistenceService.save(q);
                }
            }

            for (AndroidCalendarMapping mapping : mappingsToDelete) {
                Quest q = questPersistenceService.findFromAndroidCalendar(mapping);
                if (!q.isCompleted()) {
                    try {
                        delete(q);
                    } catch (CouchbaseLiteException e) {
                        postError(e);
                        return false;
                    }
                }
            }

            return true;
        });
    }

    private void deleteCalendars(Set<Long> calendarsToRemove) throws CouchbaseLiteException {
        for (Long calendarId : calendarsToRemove) {
            List<Quest> quests = questPersistenceService.findNotCompletedFromAndroidCalendar(calendarId);
            for (Quest q : quests) {
                delete(q);
            }
        }
    }

    private void savePlayer(Player player) {
        playerPersistenceService.save(player);
    }

    private void saveQuests(List<Quest> quests) {
        for (Quest quest : quests) {
            questPersistenceService.save(quest);
        }
    }

    private void delete(PersistedObject object) throws CouchbaseLiteException {
        database.getExistingDocument(object.getId()).delete();
    }

    @Override
    public void deleteAllCalendarsSync(Player player) {
        database.runInTransaction(() -> {
            try {
                deleteCalendars(player.getAndroidCalendars().keySet());
            } catch (CouchbaseLiteException e) {
                postError(e);
                return false;
            }
            player.setAndroidCalendars(new HashMap<>());
            savePlayer(player);
            return true;
        });
    }

    private void runAsyncTransaction(Transaction transaction) {
        database.runAsync(db -> db.runInTransaction(transaction::run));
    }

    private void postError(Exception e) {
        eventBus.post(new AppErrorEvent(e));
    }

    protected interface Transaction {
        boolean run();
    }
}
