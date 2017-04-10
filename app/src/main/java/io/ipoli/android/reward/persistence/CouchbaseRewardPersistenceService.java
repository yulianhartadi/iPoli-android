package io.ipoli.android.reward.persistence;

import com.couchbase.lite.Database;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.app.persistence.OnDataChangedListener;
import io.ipoli.android.reward.data.Reward;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/4/17.
 */
public class CouchbaseRewardPersistenceService extends BaseCouchbasePersistenceService<Reward> implements RewardPersistenceService {

    private final View allRewardsView;

    public CouchbaseRewardPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        super(database, objectMapper, eventBus);

        allRewardsView = database.getView("rewards/all");
        if (allRewardsView.getMap() == null) {
            allRewardsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Reward.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, Constants.DEFAULT_VIEW_VERSION);
        }

    }

    @Override
    protected Class<Reward> getModelClass() {
        return Reward.class;
    }

    @Override
    public void listenForAll(OnDataChangedListener<List<Reward>> listener) {
        LiveQuery query = allRewardsView.createQuery().toLiveQuery();
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                postResult(listener, getResult(event));
            }
        };
        startLiveQuery(query, changeListener);
    }
}