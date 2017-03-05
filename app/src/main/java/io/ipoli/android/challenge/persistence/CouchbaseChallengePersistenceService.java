package io.ipoli.android.challenge.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/5/17.
 */
public class CouchbaseChallengePersistenceService extends BaseCouchbasePersistenceService<Challenge> implements ChallengePersistenceService {

    private final View allChallengesView;

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
