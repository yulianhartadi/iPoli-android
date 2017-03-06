package io.ipoli.android.player.persistence;

import com.couchbase.lite.Database;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/6/17.
 */
public class CouchbasePlayerPersistenceService extends BaseCouchbasePersistenceService<Player> implements PlayerPersistenceService {
    private final String playerId;

    public CouchbasePlayerPersistenceService(Database database, ObjectMapper objectMapper, LocalStorage localStorage) {
        super(database, objectMapper);
        playerId = localStorage.readString(Constants.KEY_PLAYER_ID);
    }

    @Override
    public Player get() {
        return toObject(database.getExistingDocument(playerId).getProperties());
    }

    @Override
    public void listen(OnDataChangedListener<Player> listener) {
        listenById(playerId, listener);
    }

    @Override
    protected Class<Player> getModelClass() {
        return Player.class;
    }
}
