package io.ipoli.android.app.persistence;

import android.util.Pair;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.AndroidCalendarMapping;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.data.SourceMapping;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/10/17.
 */

public class AndroidCalendarPersistenceService implements CalendarPersistenceService {
    private final Database database;
    private final PlayerPersistenceService playerPersistenceService;
    private final QuestPersistenceService questPersistenceService;
    private final RepeatingQuestPersistenceService repeatingQuestPersistenceService;
    private final Bus eventBus;

    public AndroidCalendarPersistenceService(Database database, PlayerPersistenceService playerPersistenceService, QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService, Bus eventBus) {
        this.database = database;
        this.playerPersistenceService = playerPersistenceService;
        this.questPersistenceService = questPersistenceService;
        this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
        this.eventBus = eventBus;
    }

    @Override
    public void saveSync(Player player, List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests) {
        database.runInTransaction(() -> {
            saveRepeatingQuests(repeatingQuestToQuests);
            saveQuestsWithOriginalId(questToOriginalId);
            saveQuests(quests);
            savePlayer(player);
            return true;
        });
    }


    @Override
    public void updateSync(Player player, List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests, Set<Long> calendarsToRemove, Map<Long, Category> calendarsToUpdate) {
        database.runInTransaction(() -> {
            try {
                deleteCalendars(calendarsToRemove);
            } catch (CouchbaseLiteException e) {
                postError(e);
                return false;
            }

            saveRepeatingQuests(repeatingQuestToQuests);
            saveQuestsWithOriginalId(questToOriginalId);
            saveQuests(quests);
            savePlayer(player);

            for (Long calendarId : calendarsToUpdate.keySet()) {
                Category category = calendarsToUpdate.get(calendarId);
                List<RepeatingQuest> repeatingQuestsToUpdate = repeatingQuestPersistenceService.findFromAndroidCalendar(calendarId);
                List<Quest> questsToUpdate = questPersistenceService.findFromAndroidCalendar(calendarId);
                for (RepeatingQuest rq : repeatingQuestsToUpdate) {
                    rq.setCategoryType(category);
                    repeatingQuestPersistenceService.save(rq);
                }
                for (Quest q : questsToUpdate) {
                    q.setCategoryType(category);
                    questPersistenceService.save(q);
                }
            }

            return true;
        });
    }

    private void deleteCalendars(Set<Long> calendarsToRemove) throws CouchbaseLiteException {
        List<RepeatingQuest> repeatingQuestsToDelete = new ArrayList<>();
        List<Quest> questsToDelete = new ArrayList<>();
        for (Long calendarId : calendarsToRemove) {
            repeatingQuestsToDelete.addAll(repeatingQuestPersistenceService.findNotCompletedFromAndroidCalendar(calendarId));
            questsToDelete.addAll(questPersistenceService.findNotCompletedFromAndroidCalendar(calendarId));
        }

        for (RepeatingQuest rq : repeatingQuestsToDelete) {
            delete(rq);
        }

        for (Quest q : questsToDelete) {
            delete(q);
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

    private void saveQuestsWithOriginalId(Map<Quest, Long> questToOriginalId) {
        for (Quest q : questToOriginalId.keySet()) {
            if (StringUtils.isEmpty(q.getRepeatingQuestId())) {
                Long calendarId = q.getSourceMapping().getAndroidCalendarMapping().getCalendarId();
                Long eventId = questToOriginalId.get(q);
                AndroidCalendarMapping rqCalendarMapping = SourceMapping.fromGoogleCalendar(calendarId, eventId).getAndroidCalendarMapping();
                RepeatingQuest rq = repeatingQuestPersistenceService.findNotCompletedFromAndroidCalendar(rqCalendarMapping);
                if (rq != null) {
                    q.setRepeatingQuestId(rq.getId());
                }
            }
            questPersistenceService.save(q);
        }
    }

    private void saveRepeatingQuestsWithQuestsToRemove(Map<RepeatingQuest, Pair<List<Quest>, List<Quest>>> repeatingQuestToQuestsToRemoveAndCreate) throws CouchbaseLiteException {
        for (RepeatingQuest rq : repeatingQuestToQuestsToRemoveAndCreate.keySet()) {
            List<Quest> questsToRemove = repeatingQuestToQuestsToRemoveAndCreate.get(rq).first;
            List<Quest> questsToCreate = repeatingQuestToQuestsToRemoveAndCreate.get(rq).second;

            for (Quest q : questsToRemove) {
                delete(q);
            }
            saveRepeatingQuestWithQuests(rq, questsToCreate);
        }
    }

    private void saveRepeatingQuests(Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests) {
        for (Map.Entry<RepeatingQuest, List<Quest>> entry : repeatingQuestToQuests.entrySet()) {
            RepeatingQuest rq = entry.getKey();
            saveRepeatingQuestWithQuests(rq, entry.getValue());
        }
    }

    private void saveRepeatingQuestWithQuests(RepeatingQuest rq, List<Quest> questsToCreate) {
        repeatingQuestPersistenceService.save(rq);
        for (Quest q : questsToCreate) {
            q.setRepeatingQuestId(rq.getId());
            questPersistenceService.save(q);
        }
    }

    private void delete(PersistedObject object) throws CouchbaseLiteException {
        database.getExistingDocument(object.getId()).delete();
    }

    @Override
    public void updateAsync(List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, Pair<List<Quest>, List<Quest>>> repeatingQuestsToQuestsToRemoveAndCreate) {
        runAsyncTransaction(() -> {
            try {
                saveRepeatingQuestsWithQuestsToRemove(repeatingQuestsToQuestsToRemoveAndCreate);
            } catch (CouchbaseLiteException e) {
                postError(e);
                return false;
            }

            saveQuestsWithOriginalId(questToOriginalId);
            saveQuests(quests);

            return true;
        });
    }

    @Override
    public void deleteAsync(List<SourceMapping> questMappings, List<SourceMapping> repeatingQuestMappings) {
        runAsyncTransaction(() -> {
            for(SourceMapping sm : questMappings) {
                
            }
            return true;
        });
    }

    private void runAsyncTransaction(Transaction transaction) {
        database.runAsync(db -> db.runInTransaction(transaction::run));
    }

//    private <E> void postResult(TransactionCompleteListener listener) {
//        new Handler(Looper.getMainLooper()).post(() -> listener.onComplete());
//    }

    protected void postError(Exception e) {
        eventBus.post(new AppErrorEvent(e));
    }

    protected interface Transaction {
        boolean run();
    }
}
