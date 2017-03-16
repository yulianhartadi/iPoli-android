package io.ipoli.android.app.persistence;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.UnsavedRevision;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseCouchbasePersistenceService<T extends PersistedObject> implements PersistenceService<T> {

    protected final Database database;
    private final ObjectMapper objectMapper;
    private final Map<LiveQuery, LiveQuery.ChangeListener> queryToListener;
    private final Map<Document, Document.ChangeListener> documentToListener;

    public BaseCouchbasePersistenceService(Database database, ObjectMapper objectMapper) {
        this.database = database;
        this.objectMapper = objectMapper;
        this.queryToListener = new HashMap<>();
        this.documentToListener = new HashMap<>();
    }

    protected void startLiveQuery(LiveQuery query, LiveQuery.ChangeListener changeListener) {
        query.addChangeListener(changeListener);
        query.start();
        queryToListener.put(query, changeListener);
    }

    protected abstract Class<T> getModelClass();

    @Override
    public void save(T obj) {
        TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
        };
        Map<String, Object> data = objectMapper.convertValue(obj, mapTypeReference);

        if (StringUtils.isEmpty(obj.getId())) {
            try {
                Document document = database.createDocument();
                document.putProperties(data);
                obj.setId(document.getId());
                obj.setOwner(getPlayerId(obj));
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        } else {
            obj.markUpdated();
            UnsavedRevision revision = database.getExistingDocument(obj.getId()).createRevision();
            revision.setProperties(data);
            try {
                revision.save();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getPlayerId(T obj) {
        return App.getPlayerId();
    }

    protected String getPlayerId() {
        return getPlayerId(null);
    }

    @Override
    public void save(List<T> objects) {
        database.runInTransaction(() -> {
            for (T o : objects) {
                save(o);
            }
            return true;
        });
    }

    @Override
    public void findById(String id, OnDataChangedListener<T> listener) {
        if (StringUtils.isEmpty(id)) {
            listener.onDataChanged(null);
            return;
        }
        listener.onDataChanged(toObject(database.getExistingDocument(id).getProperties()));
    }

    protected T toObject(Object data) {
        return toObject(data, getModelClass());
    }

    protected <T> T toObject(Object data, Class<T> clazz) {
        if (data == null) {
            return null;
        }
        return objectMapper.convertValue(data, clazz);
    }

    @Override
    public void delete(T object) {
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
        for (Map.Entry<Document, Document.ChangeListener> entry : documentToListener.entrySet()) {
            Document doc = entry.getKey();
            doc.removeChangeListener(entry.getValue());
        }
    }

    @Override
    public void listenById(String id, OnDataChangedListener<T> listener) {
        Document.ChangeListener changeListener = event -> {
            DocumentChange change = event.getChange();
            if (change.isDeletion()) {
                listener.onDataChanged(null);
            } else {
                listener.onDataChanged(toObject(change.getAddedRevision().getProperties()));
            }
        };
        Document doc = database.getExistingDocument(id);
        doc.addChangeListener(changeListener);
        documentToListener.put(doc, changeListener);
        listener.onDataChanged(toObject(doc.getProperties()));
    }
}
