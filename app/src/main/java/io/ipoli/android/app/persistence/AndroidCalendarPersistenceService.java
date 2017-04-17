package io.ipoli.android.app.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.AndroidCalendarMapping;
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
    public void save(Player player, List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests, TransactionCompleteListener transactionCompleteListener) {
        save(player, quests, questToOriginalId, repeatingQuestToQuests, null, null, transactionCompleteListener);
    }

    @Override
    public void save(Player player, List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests, List<Quest> questsToDelete, List<RepeatingQuest> repeatingQuestsToDelete, TransactionCompleteListener listener) {
        database.runAsync(db -> db.runInTransaction(() -> {
            if(repeatingQuestsToDelete != null) {
                for(RepeatingQuest rq : repeatingQuestsToDelete) {
                    delete(rq);
                }
            }

            if(questsToDelete != null) {
                for(Quest q : questsToDelete) {
                    delete(q);
                }
            }

            saveRepeatingQuests(repeatingQuestToQuests);
            saveQuestsWithOriginalId(questToOriginalId);
            saveQuests(quests);

            playerPersistenceService.save(player);

            postResult(listener);
            return true;
        }));
    }

    private void saveQuests(List<Quest> quests) {
        for (Quest quest : quests) {
            questPersistenceService.save(quest);
        }
    }

    private void saveQuestsWithOriginalId(Map<Quest, Long> questToOriginalId) {
        for (Quest q : questToOriginalId.keySet()) {
            Long calendarId = q.getSourceMapping().getAndroidCalendarMapping().getCalendarId();
            Long eventId = questToOriginalId.get(q);
            AndroidCalendarMapping rqCalendarMapping = SourceMapping.fromGoogleCalendar(calendarId, eventId).getAndroidCalendarMapping();
            RepeatingQuest rq = repeatingQuestPersistenceService.findFromAndroidCalendar(rqCalendarMapping);
            if (rq == null) {
                continue;
            }
            q.setRepeatingQuestId(rq.getId());
            questPersistenceService.save(q);
        }
    }

    private void saveRepeatingQuests(Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests) {
        for (Map.Entry<RepeatingQuest, List<Quest>> entry : repeatingQuestToQuests.entrySet()) {
            RepeatingQuest rq = entry.getKey();
            repeatingQuestPersistenceService.save(rq);
            for (Quest q : entry.getValue()) {
                q.setRepeatingQuestId(rq.getId());
                questPersistenceService.save(q);
            }
        }
    }

    private void delete(PersistedObject object) {
        try {
            database.getExistingDocument(object.getId()).delete();
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
    }


    private  <E> void postResult(TransactionCompleteListener listener) {
        new Handler(Looper.getMainLooper()).post(() -> listener.onComplete());
    }

    protected void postError(Exception e) {
        eventBus.post(new AppErrorEvent(e));
    }
}
