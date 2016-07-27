package io.ipoli.android.app.persistence;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;

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
        DatabaseReference objRef = collectionRef.push();
        objRef.setValue(obj);
        obj.setId(objRef.getKey());
    }

    @Override
    public void save(List<T> objects) {

    }

    @Override
    public T findById(String id) {
        return null;
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

    protected abstract String getCollectionName();

    protected DatabaseReference getPlayerReference() {
        return database.getReference("players").child(playerId);
    }
}
