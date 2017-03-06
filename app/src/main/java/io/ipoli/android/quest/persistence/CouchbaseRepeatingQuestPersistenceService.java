package io.ipoli.android.quest.persistence;

import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.QuestData;
import io.ipoli.android.quest.data.RepeatingQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/5/17.
 */

public class CouchbaseRepeatingQuestPersistenceService extends BaseCouchbasePersistenceService<RepeatingQuest> implements RepeatingQuestPersistenceService {

    private final View allRepeatingQuestsView;
    private final QuestPersistenceService questPersistenceService;
    private final View repeatingQuestWithQuestsView;

    public CouchbaseRepeatingQuestPersistenceService(Database database, ObjectMapper objectMapper, QuestPersistenceService questPersistenceService) {
        super(database, objectMapper);

        this.questPersistenceService = questPersistenceService;

        allRepeatingQuestsView = database.getView("repeatingQuests/all");
        if (allRepeatingQuestsView.getMap() == null) {
            allRepeatingQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (RepeatingQuest.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
        }

        repeatingQuestWithQuestsView = database.getView("repeatingQuests/withQuests");
        if (repeatingQuestWithQuestsView.getMap() == null) {
            repeatingQuestWithQuestsView.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (RepeatingQuest.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                } else if (Quest.TYPE.equals(type) && document.containsKey("repeatingQuestId")) {
                    emitter.emit(document.get("repeatingQuestId"), document);
                }
            }, (keys, values, rereduce) -> {
                RepeatingQuest repeatingQuest = null;
                List<Quest> quests = new ArrayList<>();
                for (Object v : values) {
                    Map<String, Object> data = (Map<String, Object>) v;
                    if (RepeatingQuest.TYPE.equals(data.get("type"))) {
                        repeatingQuest = toObject(data);
                    } else {
                        quests.add(toObject(data, Quest.class));
                    }

                }
                return new Pair<>(repeatingQuest, quests);
            }, "1.0");
        }
    }

    @Override
    public void listenById(String id, OnDataChangedListener<RepeatingQuest> listener) {
        LiveQuery query = repeatingQuestWithQuestsView.createQuery().toLiveQuery();
        query.setStartKey(id);
        query.setEndKey(id);
        query.setGroupLevel(1);
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                RepeatingQuest rq = null;
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    QueryRow row = enumerator.next();
                    Pair<RepeatingQuest, List<Quest>> pair = (Pair<RepeatingQuest, List<Quest>>) row.getValue();
                    rq = pair.first;
                    for (Quest q : pair.second) {
                        rq.addQuestData(q.getId(), new QuestData(q));
                    }
                }
                final RepeatingQuest result = rq;
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result));
            }
        };
        startLiveQuery(query, changeListener);
    }

    @Override
    public void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {

    }

    @Override
    public void listenForAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        LiveQuery query = repeatingQuestWithQuestsView.createQuery().toLiveQuery();
        query.setGroupLevel(1);
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                List<RepeatingQuest> result = new ArrayList<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    QueryRow row = enumerator.next();
                    Pair<RepeatingQuest, List<Quest>> pair = (Pair<RepeatingQuest, List<Quest>>) row.getValue();
                    RepeatingQuest rq = pair.first;
                    for (Quest q : pair.second) {
                        rq.addQuestData(q.getId(), new QuestData(q));
                    }
                    result.add(rq);
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
    public void updateChallengeId(List<RepeatingQuest> repeatingQuests) {

    }

    @Override
    public void saveScheduledRepeatingQuests(Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests) {

    }

    @Override
    public void saveWithQuests(Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests) {
        database.runInTransaction(() -> {
            for (Map.Entry<RepeatingQuest, List<Quest>> entry : repeatingQuestToScheduledQuests.entrySet()) {
                RepeatingQuest rq = entry.getKey();
                save(rq);
                for (Quest q : entry.getValue()) {
                    q.setRepeatingQuestId(rq.getId());
                    questPersistenceService.save(q);
                }
            }
            return true;
        });
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
