package io.ipoli.android.app.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/25/16.
 */
public abstract class BaseFirebasePersistenceService<T extends PersistedObject> implements PersistenceService<T> {

    protected final FirebaseDatabase database;
    protected final String playerId;
    protected final Bus eventBus;
    protected Map<DatabaseReference, ValueEventListener> valueListeners;

    public BaseFirebasePersistenceService(Context context, Bus eventBus) {
        this.eventBus = eventBus;
        this.database = FirebaseDatabase.getInstance();
        this.playerId = LocalStorage.of(context).readString(Constants.KEY_PLAYER_REMOTE_ID);
        this.valueListeners = new HashMap<>();
    }

    @Override
    public void save(T obj) {
        DatabaseReference collectionRef = getPlayerReference().child(getCollectionName());
        DatabaseReference objRef = StringUtils.isEmpty(obj.getId()) ?
                collectionRef.push() :
                collectionRef.child(obj.getId());
        objRef.setValue(obj);
        obj.setId(objRef.getKey());
    }

    @Override
    public void save(List<T> objects) {

    }

    @Override
    public void findById(String id, OnDatabaseChangedListener<T> listener) {
        DatabaseReference dbRef = getPlayerReference().child(getCollectionName()).child(id);
//        Query query = dbRef.orderByChild("isDeleted").equalTo(false);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                T foundObject = dataSnapshot.getValue(getModelClass());
                if (foundObject != null) {
                    foundObject.setId(id);
                }
                listener.onDatabaseChanged(foundObject);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        dbRef.addListenerForSingleValueEvent(valueEventListener);
    }

    @Override
    public void delete(List<T> objects) {

    }

    @Override
    public void removeAllListeners() {
        for (DatabaseReference ref : valueListeners.keySet()) {
            ref.removeEventListener(valueListeners.get(ref));
        }
    }

    protected abstract Class<T> getModelClass();

    protected abstract String getCollectionName();

    protected DatabaseReference getPlayerReference() {
        return database.getReference("players").child(playerId);
    }
}
