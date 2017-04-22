package io.ipoli.android.player.persistence;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import java.util.Map;

import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.app.persistence.OnDataChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/6/17.
 */
public class CouchbasePlayerPersistenceService extends BaseCouchbasePersistenceService<Player> implements PlayerPersistenceService {

    public CouchbasePlayerPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        super(database, objectMapper, eventBus);
    }

    @Override
    public Player get() {
        Document player = database.getExistingDocument(getPlayerId());
        return player != null ? toObject(player.getProperties()) : null;
    }

    @Override
    public void listen(OnDataChangedListener<Player> listener) {
        listenById(getPlayerId(), listener);
    }

    @Override
    public void deletePlayer() {
        Query query = database.createAllDocumentsQuery();
        try {
            QueryEnumerator enumerator = query.run();
            while (enumerator.hasNext()) {
                enumerator.next().getDocument().purge();
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
    }

    @Override
    public void save(Player player, String playerId) {
        Document playerDoc = StringUtils.isEmpty(playerId) ? database.createDocument() : database.getDocument(playerId);

        player.setId(playerDoc.getId());
        player.setOwner(playerDoc.getId());

        TypeReference<Map<String, Object>> mapTypeReference = new TypeReference<Map<String, Object>>() {
        };
        Map<String, Object> data = objectMapper.convertValue(player, mapTypeReference);
        try {
            playerDoc.putProperties(data);
        } catch (CouchbaseLiteException e) {
            postError(e);
        }

    }

    @Override
    protected Class<Player> getModelClass() {
        return Player.class;
    }
}
