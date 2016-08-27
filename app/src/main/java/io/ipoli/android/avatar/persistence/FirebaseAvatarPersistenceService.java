package io.ipoli.android.avatar.persistence;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseAvatarPersistenceService extends BaseFirebasePersistenceService<Avatar> implements AvatarPersistenceService {

    public FirebaseAvatarPersistenceService(Bus eventBus, Gson gson) {
        super(eventBus, gson);
    }

    @Override
    public void find(OnDataChangedListener<Avatar> listener) {
        listenForSingleChange(getCollectionReference(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Avatar> data = getListFromMapSnapshot(dataSnapshot);
                if (data == null || data.isEmpty()) {
                    listener.onDataChanged(null);
                    return;
                }
                listener.onDataChanged(data.get(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void listen(OnDataChangedListener<Avatar> listener) {
        listenForQuery(getCollectionReference(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Avatar> data = getListFromMapSnapshot(dataSnapshot);
                if (data == null || data.isEmpty()) {
                    listener.onDataChanged(null);
                    return;
                }
                listener.onDataChanged(data.get(0));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        }, listener);
    }

    @Override
    protected Class<Avatar> getModelClass() {
        return Avatar.class;
    }

    @Override
    protected String getCollectionName() {
        return "avatars";
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    protected GenericTypeIndicator<Map<String, Avatar>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Avatar>>() {

        };
    }

    @Override
    protected GenericTypeIndicator<List<Avatar>> getGenericListIndicator() {
        return new GenericTypeIndicator<List<Avatar>>() {
        };
    }
}
