package io.ipoli.android.app.persistence;

import android.content.Context;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.persistence.OnChangeListener;
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseFirebasePersistenceService<T extends PersistedObject> implements PersistenceService<T> {

    protected final FirebaseDatabase database;
    protected final String playerId;
    protected final Bus eventBus;
    protected Map<DatabaseReference, ValueEventListener> valueListeners;
    protected Map<DatabaseReference, ChildEventListener> childListeners;

    public BaseFirebasePersistenceService(Context context, Bus eventBus) {
        this.eventBus = eventBus;
        this.database = FirebaseDatabase.getInstance();
        this.playerId = LocalStorage.of(context).readString(Constants.KEY_PLAYER_REMOTE_ID);
        this.valueListeners = new HashMap<>();
        this.childListeners = new HashMap<>();
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
    public void save(List<T> objects) {
        for (T obj : objects) {
            save(obj);
        }
    }

    @Override
    public void findById(String id, OnDataChangedListener<T> listener) {
        DatabaseReference dbRef = getPlayerReference().child(getCollectionName()).child(id);
        listenForSingleModelChange(dbRef, listener);
    }

    @Override
    public void delete(T object) {
        getCollectionReference().child(object.getId()).removeValue();
    }

    @Override
    public void delete(List<T> objects) {
        for (T obj : objects) {
            delete(obj);
        }
    }

    @Override
    public void removeAllListeners() {
        for (DatabaseReference ref : valueListeners.keySet()) {
            ref.removeEventListener(valueListeners.get(ref));
        }
    }

    @Override
    public void listenForChange(OnChangeListener<List<T>> listener) {
        Query query = getCollectionReference().orderByChild("updatedAt/time").startAt(new Date().getTime());
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
        childListeners.put(query.getRef(), childListener);
        query.addChildEventListener(childListener);
    }

    protected abstract Class<T> getModelClass();

    protected abstract String getCollectionName();

    protected abstract DatabaseReference getCollectionReference();

    protected DatabaseReference getPlayerReference() {
        return database.getReference("players").child(playerId);
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener) {
        listenForQuery(query, createListListener(listener));
    }

    protected void listenForListChange(Query query, OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter) {
        listenForQuery(query, createListListener(listener, queryFilter));
    }

    protected void listenForModelChange(Query query, OnDataChangedListener<T> listener) {
        listenForQuery(query, createModelListener(listener));
    }

    protected void listenForQuery(Query query, ValueEventListener valueListener) {
        valueListeners.put(query.getRef(), valueListener);
        query.addValueEventListener(valueListener);
    }

    protected void listenForSingleChange(Query query, ValueEventListener valueListener) {
        query.addListenerForSingleValueEvent(valueListener);
    }

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener, QueryFilter<T> queryFilter) {
        query.addListenerForSingleValueEvent(createListListener(listener, queryFilter));
    }

    protected void listenForSingleListChange(Query query, OnDataChangedListener<List<T>> listener) {
        listenForSingleListChange(query, listener, null);
    }

    protected void listenForSingleModelChange(Query query, OnDataChangedListener<T> listener) {
        query.addListenerForSingleValueEvent(createModelListener(listener));
    }

    protected void listenForCountChange(Query query, OnDataChangedListener<Long> listener) {
        listenForQuery(query, createCountListener(listener));
    }

    protected void listenForCountChange(Query query, OnDataChangedListener<Long> listener, QueryFilter<T> queryFilter) {
        listenForQuery(query, createCountListener(listener, queryFilter));
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

    public interface QueryFilter<T> {
        Observable<T> filter(Observable<T> data);
    }
}
