package io.ipoli.android.player.persistence;

import android.content.Context;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebasePlayerPersistenceService implements PlayerPersistenceService {

    protected final FirebaseDatabase database;
    protected final String playerId;
    protected final Bus eventBus;
    protected Map<DatabaseReference, ValueEventListener> valueListeners;

    public FirebasePlayerPersistenceService(Context context, Bus eventBus) {
        this.eventBus = eventBus;
        this.database = FirebaseDatabase.getInstance();
        this.playerId = LocalStorage.of(context).readString(Constants.KEY_PLAYER_REMOTE_ID);
        this.valueListeners = new HashMap<>();
    }

    @Override
    public void save(Player player) {
        DatabaseReference playersRef = database.getReference("players");
        DatabaseReference playerRef = StringUtils.isEmpty(player.getId()) ?
                playersRef.push() :
                playersRef.child(playerId);
        playerRef.setValue(player);
        player.setId(playerRef.getKey());
    }

    @Override
    public void find(OnDatabaseChangedListener<Player> listener) {
        DatabaseReference playerRef = database.getReference("players").child(playerId);
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

    @Override
    public void listenForChanges(OnDatabaseChangedListener<Player> listener) {
        DatabaseReference playerRef = database.getReference("players").child(playerId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onDatabaseChanged(dataSnapshot.getValue(Player.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        valueListeners.put(playerRef, valueEventListener);
        playerRef.addValueEventListener(valueEventListener);
    }

    @Override
    public void removeAllListeners() {
        for (DatabaseReference ref : valueListeners.keySet()) {
            ref.removeEventListener(valueListeners.get(ref));
        }
    }
}
