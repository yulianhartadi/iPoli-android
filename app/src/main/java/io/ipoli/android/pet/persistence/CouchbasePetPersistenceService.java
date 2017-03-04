package io.ipoli.android.pet.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.View;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/4/17.
 */

public class CouchbasePetPersistenceService implements PetPersistenceService {

    private final Database database;
    private final ObjectMapper objectMapper;
    private final LocalStorage localStorage;

    private final Map<LiveQuery, LiveQuery.ChangeListener> queryToListener;
    private final View allPetsView;

    public CouchbasePetPersistenceService(Database database, ObjectMapper objectMapper, LocalStorage localStorage) {
        this.database = database;
        this.objectMapper = objectMapper;
        this.localStorage = localStorage;

        allPetsView = database.getView("pets/all");
        if (allPetsView.getMap() == null) {
            allPetsView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (Pet.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, "1.0");
        }

        queryToListener = new HashMap<>();
    }

    @Override
    public void save(Pet pet) {
        Map<String, Object> data = new HashMap<>();
        Document document;
        if (StringUtils.isEmpty(pet.getId())) {
            document = database.createDocument();
        } else {
            document = database.getExistingDocument(pet.getId());
            data.putAll(document.getProperties());
            pet.markUpdated();
        }

        TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
        };
        data.putAll(objectMapper.convertValue(pet, mapTypeReference));

        try {
            document.putProperties(data);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        localStorage.saveInt(Constants.KEY_XP_BONUS_PERCENTAGE, pet.getExperienceBonusPercentage());
        localStorage.saveInt(Constants.KEY_COINS_BONUS_PERCENTAGE, pet.getCoinsBonusPercentage());
    }

    @Override
    public void findById(String id, OnDataChangedListener<Pet> listener) {
        listener.onDataChanged(toObject(database.getExistingDocument(id).getProperties(), Pet.class));
    }


    private <T> T toObject(Object data, Class<T> clazz) {
        return objectMapper.convertValue(data, clazz);
    }

    @Override
    public void listenById(String id, OnDataChangedListener<Pet> listener) {

    }

    @Override
    public void delete(Pet object) {
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
    public void find(OnDataChangedListener<Pet> listener) {
        Query query = allPetsView.createQuery();
        try {
            QueryEnumerator enumerator = query.run();
            List<Pet> result = new ArrayList<>();
            while (enumerator.hasNext()) {
                result.add(toObject(enumerator.next().getValue(), Pet.class));
            }
            listener.onDataChanged(result.get(0));
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void listen(OnDataChangedListener<Pet> listener) {
        LiveQuery query = allPetsView.createQuery().toLiveQuery();
        LiveQuery.ChangeListener changeListener = event -> {
            if (event.getSource().equals(query)) {
                List<Pet> result = new ArrayList<>();
                QueryEnumerator enumerator = event.getRows();
                while (enumerator.hasNext()) {
                    result.add(toObject(enumerator.next().getValue(), Pet.class));
                }
                new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result.get(0)));

            }
        };
        query.addChangeListener(changeListener);
        query.start();
        queryToListener.put(query, changeListener);
    }
}
