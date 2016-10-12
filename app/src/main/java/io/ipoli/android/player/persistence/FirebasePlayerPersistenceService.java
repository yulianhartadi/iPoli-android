package io.ipoli.android.player.persistence;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.Constants;
import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.player.Player;
import io.ipoli.android.quest.persistence.OnOperationCompletedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebasePlayerPersistenceService extends BaseFirebasePersistenceService<Player> implements PlayerPersistenceService {

    public FirebasePlayerPersistenceService(Bus eventBus, Gson gson) {
        super(eventBus, gson);
    }

    @Override
    public void save(Player obj) {
        save(obj, null);
    }

    @Override
    public void save(Player player, OnOperationCompletedListener listener) {
        DatabaseReference collectionRef = getCollectionReference();
        boolean isNew = StringUtils.isEmpty(player.getId());
        if (isNew) {
            DatabaseReference objRef = collectionRef.push();
            player.setId(objRef.getKey());

            objRef.setValue(player);
            DatabaseReference petsRef = objRef.child("pets");
            DatabaseReference petRef = petsRef.push();
            player.getPet().setId(petRef.getKey());

            petRef.setValue(player.getPet());
            DatabaseReference avatarsRef = objRef.child("avatars");
            DatabaseReference avatarRef = avatarsRef.push();
            player.getAvatar().setId(avatarRef.getKey());
            avatarRef.setValue(player.getAvatar());
            FirebaseCompletionListener.listen(listener);
        } else {
            player.markUpdated();
            DatabaseReference objRef = collectionRef.child(player.getId());
            Map<String, Object> playerData = new HashMap<>();
            playerData.put("uid", player.getUid());
            playerData.put("updatedAt", player.getUpdatedAt());
            playerData.put("createdAt", player.getCreatedAt());
            objRef.updateChildren(playerData);
            FirebaseCompletionListener.listen(listener);
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

    @Override
    protected GenericTypeIndicator<List<Player>> getGenericListIndicator() {
        return new GenericTypeIndicator<List<Player>>() {
        };
    }
}
