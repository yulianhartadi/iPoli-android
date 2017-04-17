package io.ipoli.android.app.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.Database;

import java.util.List;
import java.util.Map;

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

    public AndroidCalendarPersistenceService(Database database, PlayerPersistenceService playerPersistenceService, QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        this.database = database;
        this.playerPersistenceService = playerPersistenceService;
        this.questPersistenceService = questPersistenceService;
        this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
    }

    @Override
    public void save(Player player, List<Quest> quests, Map<Quest, Long> questToOriginalId, Map<RepeatingQuest, List<Quest>> repeatingQuestToQuests, TransactionCompleteListener transactionCompleteListener) {
        database.runAsync(db -> db.runInTransaction(() -> {
            for (Map.Entry<RepeatingQuest, List<Quest>> entry : repeatingQuestToQuests.entrySet()) {
                RepeatingQuest rq = entry.getKey();
                repeatingQuestPersistenceService.save(rq);
                for (Quest q : entry.getValue()) {
                    q.setRepeatingQuestId(rq.getId());
                    questPersistenceService.save(q);
                }
            }

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

            for (Quest quest : quests) {
                questPersistenceService.save(quest);
            }

            playerPersistenceService.save(player);

            postResult(transactionCompleteListener);
            return true;
        }));
    }

    @Override
    public void updateCalendars(List<Long> calendarsToRemove, List<Long> calendarsToAdd) {
        database.runAsync(db -> db.runInTransaction(() -> {
            for(Long calendarId : calendarsToRemove) {
                List<RepeatingQuest> repeatingQuests = repeatingQuestPersistenceService.findNotCompletedFromAndroidCalendar(calendarId);
            }
            return true;
        }));
    }

    private  <E> void postResult(TransactionCompleteListener listener) {
        new Handler(Looper.getMainLooper()).post(() -> listener.onComplete());
    }
}
