package io.ipoli.android.app.persistence;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseCouchbasePersistenceService<T extends PersistedObject> implements PersistenceService<T> {

    protected final Database database;
    protected final ObjectMapper objectMapper;
    protected final Map<LiveQuery, LiveQuery.ChangeListener> queryToListener;
    protected final Map<Document, Document.ChangeListener> documentToListener;


    public BaseCouchbasePersistenceService(Database database, ObjectMapper objectMapper) {
        this.database = database;
        this.objectMapper = objectMapper;
        this.queryToListener = new HashMap<>();
        this.documentToListener = new HashMap<>();
    }

    protected abstract Class<T> getModelClass();

    @Override
    public void save(T obj) {
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
    public void findById(String id, OnDataChangedListener<T> listener) {
        listener.onDataChanged(toObject(database.getExistingDocument(id).getProperties()));
    }

    protected T toObject(Object data) {
        return  toObject(data, getModelClass());
    }

    protected <T> T toObject(Object data, Class<T> clazz) {
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

//    @Override
//    public void save(T obj) {
//        DatabaseReference collectionRef = getCollectionReference();
//        boolean isNew = StringUtils.isEmpty(obj.getId());
//        if (!isNew) {
//            obj.markUpdated();
//        }
//        DatabaseReference objRef = isNew ?
//                collectionRef.push() :
//                collectionRef.child(obj.getId());
//        obj.setId(objRef.getKey());
//        objRef.setValue(obj);
//    }
//
//    @Override
//    public void findById(String id, OnDataChangedListener<T> listener) {
//        if (StringUtils.isEmpty(id)) {
//            listener.onDataChanged(null);
//            return;
//        }
//        DatabaseReference dbRef = getPlayerReference().child(getCollectionName()).child(id);
//        listenForSingleModelChange(dbRef, listener);
//    }
//
//    @Override
//    public void listenById(String id, OnDataChangedListener<T> listener) {
//        if (StringUtils.isEmpty(id)) {
//            listener.onDataChanged(null);
//            return;
//        }
//        DatabaseReference dbRef = getPlayerReference().child(getCollectionName()).child(id);
//        listenForModelChange(dbRef, listener);
//    }
//
//    @Override
//    public void delete(T object) {
//        getCollectionReference().child(object.getId()).removeValue();
//    }
//
//    @Override
//    public void removeAllListeners() {
//        for (ValueEventListener valueEventListener : valueListeners.keySet()) {
//            Query query = valueListeners.get(valueEventListener);
//            query.removeEventListener(valueEventListener);
//        }
//        valueListeners.clear();
//
//        for (ChildEventListener childEventListener : childListeners.keySet()) {
//            Query query = childListeners.get(childEventListener);
//            query.removeEventListener(childEventListener);
//        }
//        childListeners.clear();
//    }
//
//    @Override
//    public void removeDataChangedListener(OnDataChangedListener<?> listener) {
//        if (!listenerToValueListener.containsKey(listener)) {
//            return;
//        }
//        ValueEventListener valueEventListener = listenerToValueListener.get(listener);
//        Query query = valueListeners.get(valueEventListener);
//        query.removeEventListener(valueEventListener);
//
//        valueListeners.remove(valueEventListener);
//        listenerToValueListener.remove(listener);
//    }
//
//    protected abstract Class<T> getModelClass();
//
//    protected abstract String getCollectionName();
//
//    protected abstract DatabaseReference getCollectionReference();
//
//    protected DatabaseReference getPlayerReference() {
//        if (playerRef == null) {
//            playerRef = database.getReference(Constants.API_VERSION).child("players").child(App.getPlayerId());
//        }
//        return playerRef;
//    }
//
//    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener) {
//        listenForQuery(query, createListListener(listener), listener);
//    }
//
//    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
//        listenForQuery(query, createListListener(listener, predicate), listener);
//    }
//
//    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
//        listenForQuery(query, createSortedListListener(listener, predicate, querySort), listener);
//    }
//
//    protected void listenForModelChange(Query query, OnDataChangedListener<T> listener) {
//        listenForQuery(query, createModelListener(listener), listener);
//    }
//
//    protected void listenForQuery(Query query, ValueEventListener valueListener, OnDataChangedListener<?> listener) {
//        listenerToValueListener.put(listener, valueListener);
//        valueListeners.put(valueListener, query);
//        query.addValueEventListener(valueListener);
//    }
//
//    protected void listenForSingleChange(Query query, ValueEventListener valueListener) {
//        query.addListenerForSingleValueEvent(valueListener);
//    }
//
//    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
//        query.addListenerForSingleValueEvent(createListListener(listener, predicate));
//    }
//
//    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
//        query.addListenerForSingleValueEvent(createSortedListListener(listener, predicate, querySort));
//    }
//
//    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener) {
//        listenForSingleListChange(query, listener, null);
//    }
//
//    protected void listenForSingleModelChange(Query query, OnDataChangedListener<T> listener) {
//        query.addListenerForSingleValueEvent(createModelListener(listener));
//    }
//
//    protected void listenForSingleCountChange(Query query, OnDataChangedListener<Long> listener, Predicate<T> predicate) {
//        query.addListenerForSingleValueEvent(createCountListener(listener, predicate));
//    }
//
//    protected List<T> getListFromMapSnapshot(DataSnapshot dataSnapshot) {
//        if (dataSnapshot.getChildrenCount() == 0) {
//            return new ArrayList<>();
//        }
//        return new ArrayList<>(dataSnapshot.getValue(getGenericMapIndicator()).values());
//    }
//
//
//    protected abstract GenericTypeIndicator<Map<String, T>> getGenericMapIndicator();
//
//    private ValueEventListener createListListener(OnDataChangedListener<List<T>> listener) {
//        return createListListener(listener, null);
//    }
//
//    private ValueEventListener createListListener(OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
//        return new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                List<T> data = getListFromMapSnapshot(dataSnapshot);
//                if (predicate == null) {
//                    listener.onDataChanged(data);
//                    return;
//                }
//                listener.onDataChanged(QueryFilter.filter(data, predicate));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//    }
//
//    private ValueEventListener createSortedListListener(OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
//        return new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                List<T> data = getListFromMapSnapshot(dataSnapshot);
//                if (predicate != null) {
//                    data = QueryFilter.filter(data, predicate);
//                }
//
//                if (querySort != null) {
//                    Collections.sort(data, querySort::sort);
//                }
//                listener.onDataChanged(data);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//    }
//
//    private ValueEventListener createCountListener(OnDataChangedListener<Long> listener, Predicate<T> predicate) {
//        return new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (predicate == null) {
//
//                    listener.onDataChanged(dataSnapshot.getChildrenCount());
//                    return;
//                }
//                List<T> data = getListFromMapSnapshot(dataSnapshot);
//                List<T> filteredData = QueryFilter.filter(data, predicate);
//                listener.onDataChanged((long) filteredData.size());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//    }
//
//    private ValueEventListener createModelListener(OnDataChangedListener<T> listener) {
//        return new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                listener.onDataChanged(dataSnapshot.getValue(getModelClass()));
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//    }
//
//    protected interface Predicate<T> {
//        boolean shouldInclude(T obj);
//    }
//
//    private static class QueryFilter<T> {
//
//        public static <T> List<T> filter(List<T> data, Predicate<T> predicate) {
//            QueryFilter<T> queryFilter = new QueryFilter<>();
//            return queryFilter.filterData(data, predicate);
//        }
//
//        private List<T> filterData(List<T> data, Predicate<T> predicate) {
//            List<T> result = new ArrayList<>();
//            for (T obj : data) {
//                if (predicate.shouldInclude(obj)) {
//                    result.add(obj);
//                }
//            }
//            return result;
//        }
//    }
//
//    protected interface QuerySort<T> {
//        int sort(T obj1, T obj2);
//    }
}
