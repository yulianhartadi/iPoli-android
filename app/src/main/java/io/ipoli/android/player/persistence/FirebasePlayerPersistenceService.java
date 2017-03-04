package io.ipoli.android.player.persistence;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.Player;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebasePlayerPersistenceService extends BaseFirebasePersistenceService<Player> implements PlayerPersistenceService {

    public FirebasePlayerPersistenceService(Bus eventBus) {
        super(eventBus);
    }

    @Override
    public void save(Player player) {
        DatabaseReference collectionRef = getCollectionReference();
        boolean isNew = StringUtils.isEmpty(player.getId());
        if (isNew) {
            DatabaseReference objRef = collectionRef.push();
            player.setId(objRef.getKey());

            DatabaseReference petsRef = objRef.child("pets");
            DatabaseReference petRef = petsRef.push();
//            player.getPet().setId(petRef.getKey());

            DatabaseReference avatarsRef = objRef.child("avatars");
            DatabaseReference avatarRef = avatarsRef.push();
            player.getAvatar().setId(avatarRef.getKey());

            Map<String, Object> data = new HashMap<>();
            data.put("/id", player.getId());
            data.put("/avatars/" + player.getAvatar().getId(), player.getAvatar());
//            data.put("/pets/" + player.getPet().getId(), player.getPet());
            data.put("/schemaVersion", player.getSchemaVersion());
            data.put("/updatedAt", player.getUpdatedAt());
            data.put("/createdAt", player.getCreatedAt());
            objRef.updateChildren(data);
        } else {
            player.markUpdated();
            DatabaseReference objRef = collectionRef.child(player.getId());
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("updatedAt", player.getUpdatedAt());
            playerData.put("createdAt", player.getCreatedAt());
            objRef.updateChildren(playerData);
        }
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
        return database.getReference(Constants.API_VERSION).child(getCollectionName());
    }

    @Override
    protected GenericTypeIndicator<Map<String, Player>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Player>>() {

        };
    }
}
