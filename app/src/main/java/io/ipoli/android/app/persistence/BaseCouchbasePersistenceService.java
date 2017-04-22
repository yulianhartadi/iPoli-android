package io.ipoli.android.app.persistence;

import android.os.Handler;
import android.os.Looper;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.DocumentChange;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.View;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.app.App;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseCouchbasePersistenceService<T extends PersistedObject> implements PersistenceService<T> {

    protected final Database database;
    protected final ObjectMapper objectMapper;
    private final Bus eventBus;
    private final Map<LiveQuery, LiveQuery.ChangeListener> queryToListener;
    private final Map<Document, Document.ChangeListener> documentToListener;

    public BaseCouchbasePersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        this.database = database;
        this.objectMapper = objectMapper;
        this.eventBus = eventBus;
        this.queryToListener = new HashMap<>();
        this.documentToListener = new HashMap<>();
    }

    protected void startLiveQuery(LiveQuery query, LiveQuery.ChangeListener changeListener) {
        query.addChangeListener(changeListener);
        query.start();
        queryToListener.put(query, changeListener);
    }

    protected void runQuery(View view, OnDataChangedListener<List<T>> listener) {
        runQuery(view.createQuery(), listener, null, null);
    }

    protected void runQuery(View view, OnDataChangedListener<List<T>> listener, QuerySort<T> querySort) {
        runQuery(view.createQuery(), listener, null, querySort);
    }

    protected void runQuery(View view, OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
        runQuery(view.createQuery(), listener, predicate, null);
    }

    protected void runQuery(View view, OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
        runQuery(view.createQuery(), listener, predicate, querySort);
    }

    protected void runQuery(Query query, OnDataChangedListener<List<T>> listener) {
        runQuery(query, listener, null, null);
    }

    protected void runQuery(Query query, OnDataChangedListener<List<T>> listener, QuerySort<T> querySort) {
        runQuery(query, listener, null, querySort);
    }

    protected void runQuery(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
        listener.onDataChanged(getQueryResult(query, predicate));
    }

    protected void runQuery(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
        listener.onDataChanged(getQueryResult(query, predicate, querySort));
    }

    protected List<T> getQueryResult(Query query, Predicate<T> predicate) {
        return getQueryResult(query, predicate, null);
    }

    protected List<T> getQueryResult(Query query, Predicate<T> predicate, QuerySort<T> querySort) {
        try {
            return getResult(query.run(), predicate, querySort);
        } catch (CouchbaseLiteException e) {
            postError(e);
            return new ArrayList<>();
        }
    }

    protected List<T> getResult(LiveQuery.ChangeEvent event) {
        return getResult(event, null, null);
    }

    protected List<T> getResult(LiveQuery.ChangeEvent event, Predicate<T> predicate) {
        return getResult(event.getRows(), predicate, null);
    }

    protected List<T> getResult(LiveQuery.ChangeEvent event, QuerySort<T> querySort) {
        return getResult(event.getRows(), null, querySort);
    }

    protected List<T> getResult(QueryEnumerator enumerator, Predicate<T> predicate) {
        return getResult(enumerator, predicate, null);
    }

    protected List<T> getResult(LiveQuery.ChangeEvent event, Predicate<T> predicate, QuerySort<T> querySort) {
        return getResult(event.getRows(), predicate, querySort);
    }

    protected List<T> getResult(QueryEnumerator enumerator, Predicate<T> predicate, QuerySort<T> querySort) {
        List<T> result = new ArrayList<>();
        while (enumerator.hasNext()) {
            QueryRow row = enumerator.next();
            T obj = toObject(row.getValue());
            if (predicate == null || predicate.shouldInclude(obj)) {
                result.add(obj);
            }
        }
        if(querySort != null) {
            Collections.sort(result, querySort::sort);
        }
        return result;
    }

    protected abstract Class<T> getModelClass();

    @Override
    public void save(T obj) {
        TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
        };
        Map<String, Object> data;

        if (StringUtils.isEmpty(obj.getId())) {
            obj.setOwner(getPlayerId());
            data = objectMapper.convertValue(obj, mapTypeReference);
            try {
                Document document = database.createDocument();
                document.putProperties(data);
                obj.setId(document.getId());
            } catch (CouchbaseLiteException e) {
                postError(e);
            }
        } else {
            obj.markUpdated();
            data = objectMapper.convertValue(obj, mapTypeReference);
            UnsavedRevision revision = database.getExistingDocument(obj.getId()).createRevision();
            revision.setProperties(data);
            try {
                revision.save();
            } catch (CouchbaseLiteException e) {
                postError(e);
            }
        }
    }

    protected String getPlayerId() {
        return App.getPlayerId();
    }

    @Override
    public void save(List<T> objects) {
        runAsyncTransaction(() -> {
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

    protected <E> E toObject(Object data, Class<E> clazz) {
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
            postError(e);
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

    protected <E> void postResult(OnDataChangedListener<E> listener, E result) {
        new Handler(Looper.getMainLooper()).post(() -> listener.onDataChanged(result));
    }

    protected void postError(Exception e) {
        eventBus.post(new AppErrorEvent(e));
    }

    protected void runAsyncTransaction(Transaction transaction) {
        database.runAsync(db -> db.runInTransaction(transaction::run));
    }

    protected interface Transaction {
        boolean run();
    }

    protected interface Predicate<T> {
        boolean shouldInclude(T obj);
    }

    protected interface QuerySort<T> {
        int sort(T obj1, T obj2);
    }
}
