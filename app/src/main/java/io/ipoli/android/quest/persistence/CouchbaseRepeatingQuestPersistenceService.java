package io.ipoli.android.quest.persistence;

import android.util.Pair;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.app.persistence.OnDataChangedListener;
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

    public CouchbaseRepeatingQuestPersistenceService(Database database, ObjectMapper objectMapper, QuestPersistenceService questPersistenceService, Bus eventBus) {
        super(database, objectMapper, eventBus);

        this.questPersistenceService = questPersistenceService;

        allRepeatingQuestsView = database.getView("repeatingQuests/all");
        if (allRepeatingQuestsView.getMap() == null) {
            allRepeatingQuestsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (RepeatingQuest.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, Constants.DEFAULT_VIEW_VERSION);
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
            }, Constants.DEFAULT_VIEW_VERSION);
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
                postResult(listener, result);
            }
        };
        startLiveQuery(query, changeListener);
    }

    @Override
    public void findAllNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        runQuery(allRepeatingQuestsView, listener, rq -> rq.getCompletedAt() != null);
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
                postResult(listener, result);
            }
        };
        startLiveQuery(query, changeListener);
    }

    @Override
    public void listenForNonFlexibleNonAllDayActiveRepeatingQuests(OnDataChangedListener<List<RepeatingQuest>> listener) {
        LiveQuery query = allRepeatingQuestsView.createQuery().toLiveQuery();
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event, rq -> !rq.isFlexible() && !rq.isCompleted() && rq.shouldBeScheduledAfter(LocalDate.now())));
            }
        };
        startLiveQuery(query, changeListener);
    }

    @Override
    public void saveWithQuests(Map<RepeatingQuest, List<Quest>> repeatingQuestToScheduledQuests) {
        runAsyncTransaction(() -> {
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
    public void saveWithQuests(RepeatingQuest repeatingQuest, List<Quest> quests) {
        saveWithQuests(new HashMap<RepeatingQuest, List<Quest>>() {{
            put(repeatingQuest, quests);
        }});
    }

    @Override
    public void update(RepeatingQuest repeatingQuest, List<Quest> questsToRemove, List<Quest> questsToCreate) {
        runAsyncTransaction(() -> {
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
    public void delete(RepeatingQuest repeatingQuest) {
        runAsyncTransaction(() -> {
            Query query = repeatingQuestWithQuestsView.createQuery();
            query.setStartKey(repeatingQuest.getId());
            query.setEndKey(repeatingQuest.getId());
            query.setGroupLevel(1);
            try {
                QueryEnumerator enumerator = query.run();
                while (enumerator.hasNext()) {
                    Pair<RepeatingQuest, List<Quest>> pair = (Pair<RepeatingQuest, List<Quest>>) enumerator.next().getValue();
                    List<Quest> quests = pair.second;
                    for (Quest q : quests) {
                        if (q.isCompleted()) {
                            q.setRepeatingQuestId(null);
                            questPersistenceService.save(q);
                        } else {
                            questPersistenceService.delete(q);
                        }
                    }
                    CouchbaseRepeatingQuestPersistenceService.super.delete(pair.first);
                }
                return true;
            } catch (CouchbaseLiteException e) {
                postError(e);
                return false;
            }
        });
    }

    @Override
    public void removeFromChallenge(RepeatingQuest repeatingQuest) {
        runAsyncTransaction(() -> {
            repeatingQuest.setChallengeId(null);
            Query query = repeatingQuestWithQuestsView.createQuery();
            query.setStartKey(repeatingQuest.getId());
            query.setEndKey(repeatingQuest.getId());
            query.setGroupLevel(1);
            try {
                QueryEnumerator enumerator = query.run();
                while (enumerator.hasNext()) {
                    Pair<RepeatingQuest, List<Quest>> pair = (Pair<RepeatingQuest, List<Quest>>) enumerator.next().getValue();
                    List<Quest> quests = pair.second;
                    for (Quest q : quests) {
                        q.setChallengeId(null);
                        questPersistenceService.save(q);
                    }
                    save(repeatingQuest);
                }
                return true;
            } catch (CouchbaseLiteException e) {
                postError(e);
                return false;
            }
        });


    }

    @Override
    public void addToChallenge(List<RepeatingQuest> repeatingQuests, String challengeId) {
        runAsyncTransaction(() -> {
            try {
                for (RepeatingQuest rq : repeatingQuests) {
                    rq.setChallengeId(challengeId);

                    Query query = repeatingQuestWithQuestsView.createQuery();
                    query.setStartKey(rq.getId());
                    query.setEndKey(rq.getId());
                    query.setGroupLevel(1);

                    QueryEnumerator enumerator = query.run();
                    while (enumerator.hasNext()) {
                        Pair<RepeatingQuest, List<Quest>> pair = (Pair<RepeatingQuest, List<Quest>>) enumerator.next().getValue();
                        List<Quest> quests = pair.second;
                        for (Quest q : quests) {
                            q.setChallengeId(challengeId);
                            questPersistenceService.save(q);
                        }
                        save(rq);
                    }
                }
                return true;
            } catch (CouchbaseLiteException e) {
                postError(e);
                return false;
            }

        });
    }

    @Override
    protected Class<RepeatingQuest> getModelClass() {
        return RepeatingQuest.class;
    }
}
