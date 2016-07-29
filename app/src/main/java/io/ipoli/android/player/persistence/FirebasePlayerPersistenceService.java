package io.ipoli.android.player.persistence;

import android.content.Context;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.squareup.otto.Bus;

import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebasePlayerPersistenceService extends BaseFirebasePersistenceService<Player> implements PlayerPersistenceService {

    public FirebasePlayerPersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    public void find(OnDataChangedListener<Player> listener) {
        DatabaseReference playerRef = getCollectionReference().child(playerId);
        listenForSingleModelChange(playerRef, listener);
    }

    @Override
    public void listen(OnDataChangedListener<Player> listener) {
        DatabaseReference playerRef = getCollectionReference().child(playerId);
        listenForModelChange(playerRef, listener);
    }

    @Override
    protected Class<Player> getModelClass() {
        return Player.class;
    }

    @Override
    protected String getCollectionName() {
        return "players";
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return database.getReference(getCollectionName());
    }

    @Override
    protected GenericTypeIndicator<Map<String, Player>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Player>>() {

        };
    }
}
