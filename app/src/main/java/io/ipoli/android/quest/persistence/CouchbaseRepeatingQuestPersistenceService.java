package io.ipoli.android.quest.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/5/17.
 */

public class CouchbaseRepeatingQuestPersistenceService extends BaseCouchbasePersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    private final View allRepeatingQuestsView;
    private final Database database;
    private final ObjectMapper objectMapper;
    private final QuestPersistenceService questPersistenceService;

    public CouchbaseRepeatingQuestPersistenceService(Database database, ObjectMapper objectMapper, QuestPersistenceService questPersistenceService) {
        super(database, objectMapper);

        allRepeatingQuestsView = database.getView("repeatingQuests/all");
        this.database = database;
        this.objectMapper = objectMapper;
        this.questPersistenceService = questPersistenceService;
        if (allRepeatingQuestsView.getMap() == null) {
            allRepeatingQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (RepeatingQuest.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
        }
    }

    @Override
    public void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void listenForAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        LiveQuery query = allRepeatingQuestsView.createQuery().toLiveQuery();
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                List<RepeatingQuest> result = new ArrayList<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    result.add(toObject(enumerator.next().getValue()));
                }
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result));
            }
        };
        startLiveQuery(query, changeListener);
    }

    @Override
    public void listenForNonFlexibleNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void findActiveNotForChallenge(String query, Challenge challenge, OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void listenForActive(OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void save(RepeatingQuest repeatingQuest, List<Quest> quests) {

    }

    @Override
    public void updateChallengeId(List<RepeatingQuest> repeatingQuests) {

    }

    @Override
    public void saveScheduledRepeatingQuests(Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests) {

    }

    @Override
    public void save(Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests) {

    }

    @Override
    public void update(RepeatingQuest repeatingQuest, List<Quest> questsToRemove, List<Quest> questsToCreate) {
        database.runInTransaction(() -> {
            for (Quest q : questsToRemove) {
                questPersistenceService.delete(q);
            }
            for (Quest q : questsToCreate) {
                questPersistenceService.save(q);
            }
            save(repeatingQuest);
            return true;
        });
    }

    @Override
    public void update(RepeatingQuest repeatingQuest) {

    }

    @Override
    public void delete(RepeatingQuest repeatingQuest, List<Quest> quests) {

    }

    @Override
    protected Class<RepeatingQuest> getModelClass() {
        return RepeatingQuest.class;
    }
}
