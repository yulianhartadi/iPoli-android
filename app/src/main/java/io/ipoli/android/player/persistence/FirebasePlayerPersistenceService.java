package io.ipoli.android.player.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebasePlayerPersistenceService extends BaseFirebasePersistenceService<Player> implements PlayerPersistenceService {

    public FirebasePlayerPersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    protected String getCollectionName() {
        return "players";
    }

    @Override
    public void find(OnDatabaseChangedListener<Player> listener) {
        DatabaseReference playerRef = database.getReference(getCollectionName()).child(playerId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDatabaseChanged(dataSnapshot.getValue(Player.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        playerRef.addListenerForSingleValueEvent(valueEventListener);
    }
}
