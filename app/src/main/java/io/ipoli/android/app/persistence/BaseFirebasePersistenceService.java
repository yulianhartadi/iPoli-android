package io.ipoli.android.app.persistence;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseFirebasePersistenceService<T extends PersistedObject> implements PersistenceService<T> {

    protected final FirebaseDatabase database;
    protected final Bus eventBus;
    private final Map<ValueEventListener, Query> valueListeners;
    protected final Map<ChildEventListener, Query> childListeners;
    private final Map<OnDataChangedListener<?>, ValueEventListener> listenerToValueListener;
    private DatabaseReference playerRef;

    public BaseFirebasePersistenceService(Bus eventBus) {
        this.eventBus = eventBus;
        this.database = FirebaseDatabase.getInstance();
        this.valueListeners = new HashMap<>();
        this.childListeners = new HashMap<>();
        this.listenerToValueListener = new HashMap<>();
        this.playerRef = null;
    }

    @Override
    public void save(List<T> objects) {

    }

    @Override
    public void save(T obj) {
        DatabaseReference collectionRef = getCollectionReference();
        boolean isNew = StringUtils.isEmpty(obj.getId());
        if (!isNew) {
            obj.markUpdated();
        }
        DatabaseReference objRef = isNew ?
                collectionRef.push() :
                collectionRef.child(obj.getId());
        obj.setId(objRef.getKey());
        objRef.setValue(obj);
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
        getCollectionReference().child(object.getId()).removeValue();
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

    protected abstract Class<T> getModelClass();

    protected abstract String getCollectionName();

    protected abstract DatabaseReference getCollectionReference();

    protected DatabaseReference getPlayerReference() {
        if (playerRef == null) {
            playerRef = database.getReference(Constants.API_VERSION).child("players").child(App.getPlayerId());
        }
        return playerRef;
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener) {
        listenForQuery(query, createListListener(listener), listener);
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
        listenForQuery(query, createListListener(listener, predicate), listener);
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
        listenForQuery(query, createSortedListListener(listener, predicate, querySort), listener);
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

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
        query.addListenerForSingleValueEvent(createListListener(listener, predicate));
    }

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
        query.addListenerForSingleValueEvent(createSortedListListener(listener, predicate, querySort));
    }

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener) {
        listenForSingleListChange(query, listener, null);
    }

    protected void listenForSingleModelChange(Query query, OnDataChangedListener<T> listener) {
        query.addListenerForSingleValueEvent(createModelListener(listener));
    }

    protected void listenForSingleCountChange(Query query, OnDataChangedListener<Long> listener, Predicate<T> predicate) {
        query.addListenerForSingleValueEvent(createCountListener(listener, predicate));
    }

    protected List<T> getListFromMapSnapshot(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getChildrenCount() == 0) {
            return new ArrayList<>();
        }
        return new ArrayList<>(dataSnapshot.getValue(getGenericMapIndicator()).values());
    }


    protected abstract GenericTypeIndicator<Map<String, T>> getGenericMapIndicator();

    private ValueEventListener createListListener(OnDataChangedListener<List<T>> listener) {
        return createListListener(listener, null);
    }

    private ValueEventListener createListListener(OnDataChangedListener<List<T>> listener, Predicate<T> predicate) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<T> data = getListFromMapSnapshot(dataSnapshot);
                if (predicate == null) {
                    listener.onDataChanged(data);
                    return;
                }
                listener.onDataChanged(QueryFilter.filter(data, predicate));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener createSortedListListener(OnDataChangedListener<List<T>> listener, Predicate<T> predicate, QuerySort<T> querySort) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<T> data = getListFromMapSnapshot(dataSnapshot);
                if (predicate != null) {
                    data = QueryFilter.filter(data, predicate);
                }

                if (querySort != null) {
                    Collections.sort(data, querySort::sort);
                }
                listener.onDataChanged(data);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener createCountListener(OnDataChangedListener<Long> listener, Predicate<T> predicate) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (predicate == null) {

                    listener.onDataChanged(dataSnapshot.getChildrenCount());
                    return;
                }
                List<T> data = getListFromMapSnapshot(dataSnapshot);
                List<T> filteredData = QueryFilter.filter(data, predicate);
                listener.onDataChanged((long) filteredData.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    private ValueEventListener createModelListener(OnDataChangedListener<T> listener) {
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

    protected interface Predicate<T> {
        boolean shouldInclude(T obj);
    }

    private static class QueryFilter<T> {

        public static <T> List<T> filter(List<T> data, Predicate<T> predicate) {
            QueryFilter<T> queryFilter = new QueryFilter<>();
            return queryFilter.filterData(data, predicate);
        }

        private List<T> filterData(List<T> data, Predicate<T> predicate) {
            List<T> result = new ArrayList<>();
            for (T obj : data) {
                if (predicate.shouldInclude(obj)) {
                    result.add(obj);
                }
            }
            return result;
        }
    }

    protected interface QuerySort<T> {
        int sort(T obj1, T obj2);
    }
}
