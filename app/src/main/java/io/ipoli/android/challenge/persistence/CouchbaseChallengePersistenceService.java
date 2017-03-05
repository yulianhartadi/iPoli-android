package io.ipoli.android.challenge.persistence;

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
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/5/17.
 */
public class CouchbaseChallengePersistenceService extends BaseCouchbasePersistenceService<Challenge> implements ChallengePersistenceService {

    private final View allChallengesView;
    private final View challengesWithAllQuestsView;

    public CouchbaseChallengePersistenceService(Database database, ObjectMapper objectMapper) {
        super(database, objectMapper);

        allChallengesView = database.getView("challenges/all");
        if (allChallengesView.getMap() == null) {
            allChallengesView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Challenge.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
        }

        challengesWithAllQuestsView = database.getView("challenges/withAllQuests");
        if (challengesWithAllQuestsView.getMap() == null) {
            challengesWithAllQuestsView.setMapReduce((document, emitter) -> {
                String type = (String) document.get("type");
                if (Challenge.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                } else if (document.containsKey("challengeId") &&
                        (RepeatingQuest.TYPE.equals(type) || Quest.TYPE.equals(type))) {
                    emitter.emit(document.get("challengeId"), document);
                }
            }, (keys, values, rereduce) -> {
                Challenge challenge = null;
                List<RepeatingQuest> repeatingQuests = new ArrayList<>();
                List<Quest> quests = new ArrayList<>();

                for (Object v : values) {
                    Map<String, Object> data = (Map<String, Object>) v;
                    String type = (String) data.get("type");
                    if (Challenge.TYPE.equals(type)) {
                        challenge = toObject(data);
                    } else if (RepeatingQuest.TYPE.equals(type)) {
                        repeatingQuests.add(toObject(data, RepeatingQuest.class));
                    } else {
                        quests.add(toObject(data, Quest.class));
                    }
                }
                return new Pair<>(challenge, new Pair<>(repeatingQuests, quests));
            }, "1.0");
        }
    }

    @Override
    public void listenById(String id, OnDataChangedListener<Challenge> listener) {
        LiveQuery query = challengesWithAllQuestsView.createQuery().toLiveQuery();
        query.setStartKey(id);
        query.setEndKey(id);
        query.setGroupLevel(1);
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                Challenge ch = null;

                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    QueryRow row = enumerator.next();
                    Pair<Challenge, Pair<List<RepeatingQuest>, List<Quest>>> pair = (Pair<Challenge, Pair<List<RepeatingQuest>, List<Quest>>>) row.getValue();
                    ch = pair.first;
                    List<RepeatingQuest> repeatingQuests = pair.second.first;
                    List<Quest> quests = pair.second.second;

                    for (RepeatingQuest rq : repeatingQuests) {
                        ch.addChallengeRepeatingQuest(rq);
                    }

                    for (Quest q : quests) {
                        if (!q.isFromRepeatingQuest()) {
                            ch.addChallengeQuest(q);
                        }
                        ch.addQuestData(q.getId(), new QuestData(q));
                    }
                }
                final Challenge result = ch;
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result));
            }
        };
        startLiveQuery(query, changeListener);
    }

    @Override
    public void listenForAll(OnDataChangedListener<List<Challenge>> listener) {
        LiveQuery query = allChallengesView.createQuery().toLiveQuery();
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                List<Challenge> result = new ArrayList<>();
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
    public void deleteWithQuests(Challenge challenge, List<Quest> repeatingQuestInstances) {

    }

    @Override
    protected Class<Challenge> getModelClass() {
        return Challenge.class;
    }
}
