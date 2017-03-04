package io.ipoli.android.reward.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.View;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import io.ipoli.android.reward.data.Reward;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/4/17.
 */
public class CouchbaseRewardPersistenceService implements RewardPersistenceService {

    private final Database database;
    private final ObjectMapper objectMapper;

    private final View allRewardsView;
    private final Map<LiveQuery, LiveQuery.ChangeListener> queryToListener;

    public CouchbaseRewardPersistenceService(Database database, ObjectMapper objectMapper) {
        this.database = database;
        this.objectMapper = objectMapper;

        allRewardsView = database.getView("rewards/all");
        if (allRewardsView.getMap() == null) {
            allRewardsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Reward.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
        }

        queryToListener = new HashMap<>();
    }

    @Override
    public void save(Reward obj) {
        Map<String, Object> data = new HashMap<>();
        Document document;
        if (StringUtils.isEmpty(obj.getId())) {
            document = database.createDocument();
        } else {
            document = database.getExistingDocument(obj.getId());
            data.putAll(document.getProperties());
            obj.markUpdated();
        }

        TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
        };
        data.putAll(objectMapper.convertValue(obj, mapTypeReference));

        try {
            document.putProperties(data);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void findById(String id, OnDataChangedListener<Reward> listener) {
        listener.onDataChanged(toObject(database.getExistingDocument(id).getProperties(), Reward.class));
    }

    private <T> T toObject(Object data, Class<T> clazz) {
        return objectMapper.convertValue(data, clazz);
    }

    @Override
    public void listenById(String id, OnDataChangedListener<Reward> listener) {

    }

    @Override
    public void delete(Reward object) {
        try {
            database.getExistingDocument(object.getId()).delete();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeAllListeners() {
        for (Map.Entry<LiveQuery, LiveQuery.ChangeListener> entry : queryToListener.entrySet()) {
            LiveQuery liveQuery = entry.getKey();
            liveQuery.removeChangeListener(entry.getValue());
            liveQuery.stop();
        }
    }

    @Override
    public void removeDataChangedListener(OnDataChangedListener<?> listener) {

    }

    @Override
    public void listenForAll(OnDataChangedListener<List<Reward>> listener) {
        LiveQuery query = allRewardsView.createQuery().toLiveQuery();
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                List<Reward> result = new ArrayList<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    result.add(toObject(enumerator.next().getValue(), Reward.class));
                }
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result));
            }
        };
        query.addChangeListener(changeListener);
        query.start();
        queryToListener.put(query, changeListener);
    }
}