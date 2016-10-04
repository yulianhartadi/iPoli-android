package io.ipoli.android.app.persistence;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.otto.Bus;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.persistence.OnChangeListener;
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import io.ipoli.android.quest.persistence.OnOperationCompletedListener;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseFirebasePersistenceService<T extends PersistedObject> implements PersistenceService<T> {

    protected final FirebaseDatabase database;
    protected final Bus eventBus;
    private final Gson gson;
    private final Map<ValueEventListener, Query> valueListeners;
    protected final Map<ChildEventListener, Query> childListeners;
    private final Map<OnDataChangedListener<?>, ValueEventListener> listenerToValueListener;
    private DatabaseReference playerRef;

    public BaseFirebasePersistenceService(Bus eventBus, Gson gson) {
        this.eventBus = eventBus;
        this.gson = gson;
        this.database = FirebaseDatabase.getInstance();
        this.valueListeners = new HashMap<>();
        this.childListeners = new HashMap<>();
        this.listenerToValueListener = new HashMap<>();
        this.playerRef = null;
    }

    @Override
    public void save(T obj) {
        save(obj, null);
    }

    @Override
    public void save(T obj, OnOperationCompletedListener listener) {
        DatabaseReference collectionRef = getCollectionReference();
        boolean isNew = StringUtils.isEmpty(obj.getId());
        if (!isNew) {
            obj.markUpdated();
        }
        DatabaseReference objRef = isNew ?
                collectionRef.push() :
                collectionRef.child(obj.getId());
        obj.setId(objRef.getKey());
        objRef.setValue(obj, FirebaseCompletionListener.listen(listener));
    }

    @Override
    public void save(List<T> objects) {
        save(objects, null);
    }

    @Override
    public void save(List<T> objects, OnOperationCompletedListener listener) {
        String json = gson.toJson(objects);
        Type type = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        List<Map<String, Object>> objMaps = gson.fromJson(json, type);
        DatabaseReference collectionRef = getCollectionReference();
        Map<String, Object> data = new HashMap<>();
        for (int i = 0; i < objMaps.size(); i++) {
            Map<String, Object> objMap = objMaps.get(i);
            boolean isNew = !objMap.containsKey("id");
            if (isNew) {
                String id = collectionRef.push().getKey();
                objects.get(i).setId(id);
                objMap.put("id", id);
                data.put(id, objMap);
            } else {
                objMap.put("updatedAt", new Date().getTime());
                data.put(objMap.get("id").toString(), objMap);
            }
        }
        collectionRef.updateChildren(data, FirebaseCompletionListener.listen(listener));
    }

    @Override
    public void findById(String id, OnDataChangedListener<T> listener) {
        if (StringUtils.isEmpty(id)) {
            listener.onDataChanged(null);
            return;
        }
        DatabaseReference dbRef = getPlayerReference().child(getCollectionName()).child(id);
        listenForSingleModelChange(dbRef, listener);
    }

    @Override
    public void listenById(String id, OnDataChangedListener<T> listener) {
        if (StringUtils.isEmpty(id)) {
            listener.onDataChanged(null);
            return;
        }
        DatabaseReference dbRef = getPlayerReference().child(getCollectionName()).child(id);
        listenForModelChange(dbRef, listener);
    }

    @Override
    public void delete(T object) {
        delete(object, null);
    }

    @Override
    public void delete(T object, OnOperationCompletedListener listener) {
        getCollectionReference().child(object.getId()).removeValue(FirebaseCompletionListener.listen(listener));
    }

    @Override
    public void delete(List<T> objects) {
        delete(objects, null);
    }

    @Override
    public void delete(List<T> objects, OnOperationCompletedListener listener) {
        DatabaseReference collectionRef = getCollectionReference();
        Map<String, Object> data = new HashMap<>();
        for (T obj : objects) {
            data.put(obj.getId(), null);
        }
        collectionRef.updateChildren(data, FirebaseCompletionListener.listen(listener));
    }

    @Override
    public void removeAllListeners() {
        for (ValueEventListener valueEventListener : valueListeners.keySet()) {
            Query query = valueListeners.get(valueEventListener);
            query.removeEventListener(valueEventListener);
        }
        valueListeners.clear();

        for (ChildEventListener childEventListener : childListeners.keySet()) {
            Query query = childListeners.get(childEventListener);
            query.removeEventListener(childEventListener);
        }
        childListeners.clear();
    }

    @Override
    public void removeDataChangedListener(OnDataChangedListener<?> listener) {
        if (!listenerToValueListener.containsKey(listener)) {
            return;
        }
        ValueEventListener valueEventListener = listenerToValueListener.get(listener);
        Query query = valueListeners.get(valueEventListener);
        query.removeEventListener(valueEventListener);

        valueListeners.remove(valueEventListener);
        listenerToValueListener.remove(listener);
    }

    @Override
    public void listenForChange(OnChangeListener<List<T>> listener) {
        Query query = getCollectionReference().orderByChild("updatedAt").startAt(new Date().getTime());
        ChildEventListener childListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousName) {
                List<T> result = new ArrayList<>();
                result.add(dataSnapshot.getValue(getModelClass()));
                listener.onNew(result);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousName) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        childListeners.put(childListener, query);
        query.addChildEventListener(childListener);
    }

    protected abstract Class<T> getModelClass();

    protected abstract String getCollectionName();

    protected abstract DatabaseReference getCollectionReference();

    protected DatabaseReference getPlayerReference() {
        if(playerRef == null) {
            playerRef = database.getReference(Constants.API_VERSION).child("players").child(App.getPlayerId());
            playerRef.keepSynced(true);
        }
        return playerRef;
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener) {
        listenForQuery(query, createListListener(listener), listener);
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter) {
        listenForQuery(query, createListListener(listener, queryFilter), listener);
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter, QuerySort<T> querySort) {
        listenForQuery(query, createSortedListListener(listener, queryFilter, querySort), listener);
    }

    protected void listenForModelChange(Query query, OnDataChangedListener<T> listener) {
        listenForQuery(query, createModelListener(listener), listener);
    }

    protected void listenForQuery(Query query, ValueEventListener valueListener, OnDataChangedListener<?> listener) {
        listenerToValueListener.put(listener, valueListener);
        valueListeners.put(valueListener, query);
        query.addValueEventListener(valueListener);
    }

    protected void listenForSingleChange(Query query, ValueEventListener valueListener) {
        query.addListenerForSingleValueEvent(valueListener);
    }

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter) {
        query.addListenerForSingleValueEvent(createListListener(listener, queryFilter));
    }

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter, QuerySort<T> querySort) {
        query.addListenerForSingleValueEvent(createSortedListListener(listener, queryFilter, querySort));
    }

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener) {
        listenForSingleListChange(query, listener, null);
    }

    protected void listenForSingleModelChange(Query query, OnDataChangedListener<T> listener) {
        query.addListenerForSingleValueEvent(createModelListener(listener));
    }

    protected void listenForCountChange(Query query, OnDataChangedListener<Long> listener) {
        listenForQuery(query, createCountListener(listener), listener);
    }

    protected void listenForCountChange(Query query, OnDataChangedListener<Long> listener, QueryFilter<T> queryFilter) {
        listenForQuery(query, createCountListener(listener, queryFilter), listener);
    }

    protected void listenForSingleCountChange(Query query, OnDataChangedListener<Long> listener) {
        query.addListenerForSingleValueEvent(createCountListener(listener));
    }

    protected void listenForSingleCountChange(Query query, OnDataChangedListener<Long> listener, QueryFilter<T> queryFilter) {
        query.addListenerForSingleValueEvent(createCountListener(listener, queryFilter));
    }

    protected List<T> getListFromMapSnapshot(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() == 0) {
            return new ArrayList<>();
        }

        return new ArrayList<>(dataSnapshot.getValue(getGenericMapIndicator()).values());
    }


    protected abstract GenericTypeIndicator<Map<String, T>> getGenericMapIndicator();

    protected abstract GenericTypeIndicator<List<T>> getGenericListIndicator();

    protected ValueEventListener createListListener(OnDataChangedListener<List<T>> listener) {
        return createListListener(listener, null);
    }

    protected ValueEventListener createListListener(OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<T> data = getListFromMapSnapshot(dataSnapshot);
                if (queryFilter == null) {
                    listener.onDataChanged(data);
                    return;
                }
                List<T> filteredData = queryFilter.filter(Observable.from(data)).toList().toBlocking().single();
                listener.onDataChanged(filteredData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    protected ValueEventListener createSortedListListener(OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter, QuerySort<T> querySort) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<T> data = getListFromMapSnapshot(dataSnapshot);
                Observable<T> observableData = Observable.from(data);
                if (queryFilter != null) {
                    observableData = queryFilter.filter(observableData);
                }

                List<T> filteredData;
                if (querySort != null) {
                    filteredData = observableData.toSortedList(querySort::sort).toBlocking().single();
                } else {
                    filteredData = observableData.toSortedList().toBlocking().single();
                }
                listener.onDataChanged(filteredData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    protected ValueEventListener createCountListener(OnDataChangedListener<Long> listener) {
        return createCountListener(listener, null);
    }

    protected ValueEventListener createCountListener(OnDataChangedListener<Long> listener, QueryFilter<T> queryFilter) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (queryFilter == null) {
                    listener.onDataChanged(dataSnapshot.getChildrenCount());
                    return;
                }
                List<T> data = getListFromMapSnapshot(dataSnapshot);
                List<T> filteredData = queryFilter.filter(Observable.from(data)).toList().toBlocking().single();
                listener.onDataChanged((long) filteredData.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    protected ValueEventListener createModelListener(OnDataChangedListener<T> listener) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDataChanged(dataSnapshot.getValue(getModelClass()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private static class FirebaseCompletionListener implements DatabaseReference.CompletionListener {

        private final OnOperationCompletedListener listener;

        private FirebaseCompletionListener(OnOperationCompletedListener listener) {
            this.listener = listener;
        }

        public static FirebaseCompletionListener listen(OnOperationCompletedListener listener) {
            return new FirebaseCompletionListener(listener);
        }

        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
            if (listener != null) {
                listener.onComplete();
            }
        }
    }

    public interface QueryFilter<T> {
        Observable<T> filter(Observable<T> data);
    }

    public interface QuerySort<T> {
        int sort(T obj1, T obj2);
    }
}
