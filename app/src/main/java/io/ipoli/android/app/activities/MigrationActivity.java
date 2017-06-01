package io.ipoli.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.app.events.PlayerMigratedEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.Avatar;
import io.ipoli.android.player.PetAvatar;
import io.ipoli.android.store.Upgrade;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class MigrationActivity extends BaseActivity {

    @Inject
    Api api;

    @Inject
    Database database;

    @Inject
    Bus eventBus;

    @Inject
    LocalStorage localStorage;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migration);
        ButterKnife.bind(this);
        App.getAppComponent(this).inject(this);

        int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(flags);

        int schemaVersion = localStorage.readInt(Constants.KEY_SCHEMA_VERSION);

        int versionBeforeUpgrades = 3;

        if(schemaVersion < versionBeforeUpgrades) {
            migrateFromFirebase(() -> unlockUpgrades(() -> MigrationActivity.this.onFinish()));
        } else if(schemaVersion == versionBeforeUpgrades) {
            unlockUpgrades(() -> MigrationActivity.this.onFinish());
        } else {
            onFinish();
        }
    }

    private void unlockUpgrades(OnFinishListener onFinishListener) {
        Player player = playerPersistenceService.get();

        UnsavedRevision revision = database.getExistingDocument(player.getId()).createRevision();
        Map<String, Object> properties = revision.getProperties();
        String playerPicture = (String) properties.get("picture");
        properties.remove("picture");
        List<Map<String, Object>> pets = (List<Map<String, Object>>) properties.get("pets");
        Map<String, Object> pet = pets.get(0);
        String petPicture = (String) pet.remove("picture");

        revision.setProperties(properties);
        try {
            revision.save();
        } catch (CouchbaseLiteException e) {
            eventBus.post(new AppErrorEvent(e));
        }


        player = playerPersistenceService.get();
        LocalDate unlockedDate = DateUtils.fromMillis(player.getCreatedAt());
        for(Upgrade upgrade: Upgrade.values()) {
            player.getInventory().addUpgrade(upgrade, unlockedDate);
        }

        player.setRewardPoints(player.getCoins());
        player.setSchemaVersion(Constants.SCHEMA_VERSION);

        for(Avatar avatar : Avatar.values()) {
            String avatarPicture = getResources().getResourceEntryName(avatar.picture);
            if(avatarPicture.equals(playerPicture)) {
                player.getInventory().addAvatar(avatar, unlockedDate);
                player.setAvatar(avatar);
                break;
            }
        }

        for(PetAvatar petAvatar : PetAvatar.values()) {
            String petAvatarPicture = getResources().getResourceEntryName(petAvatar.picture);
            if(petAvatarPicture.equals(petPicture)) {
                player.getInventory().addPet(petAvatar, unlockedDate);
                player.getPet().setPetAvatar(petAvatar);
            }
        }

        playerPersistenceService.save(player);

        localStorage.saveInt(Constants.KEY_SCHEMA_VERSION, Constants.SCHEMA_VERSION);

        Toast.makeText(this, R.string.upgrades_migration_message, Toast.LENGTH_LONG).show();
        onFinishListener.onFinish();
    }

    private void migrateFromFirebase(OnFinishListener onFinishListener) {
        if (!NetworkConnectivityUtils.isConnectedToInternet(this)) {
            Toast.makeText(this, R.string.migration_no_internet, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String firebasePlayerId = App.getPlayerId();
        api.migratePlayer(firebasePlayerId, new Api.PlayerMigratedListener() {
            @Override
            public void onSuccess(Map<String, List<Map<String, Object>>> documents) {
                if (!documents.containsKey("player")) {
                    showErrorMessage(new Exception("Player with firebase id:" + firebasePlayerId + " not found"));
                    return;
                }
                Map<String, Object> player = documents.get("player").get(0);
                saveDocuments(documents, player);
                String playerId = (String) player.get("id");
                eventBus.post(new PlayerCreatedEvent(playerId));
                eventBus.post(new PlayerMigratedEvent(firebasePlayerId, playerId));
                onFinishListener.onFinish();
            }

            @Override
            public void onError(Exception e) {
                showErrorMessage(e);
            }
        });
    }

    private void saveDocuments(Map<String, List<Map<String, Object>>> documents, Map<String, Object> player) {
        database.runInTransaction(() -> {
            player.put("schemaVersion", Constants.SCHEMA_VERSION);
            save(player);

            saveProps(documents, "rewards");

            saveProps(documents, "challenges");

            saveProps(documents, "repeating_quests");

            if (documents.containsKey("quests")) {
                List<Map<String, Object>> quests = documents.get("quests");
                for (Map<String, Object> q : quests) {
                    if (q.containsKey("scheduled") && q.containsKey("startMinute") && q.containsKey("reminders")) {
                        List<Map<String, Object>> reminders = (List<Map<String, Object>>) q.get("reminders");
                        for (Map<String, Object> reminder : reminders) {
                            if (reminder.containsKey("start")) {
                                continue;
                            }
                            Time startTime = Time.of((Integer) q.get("startMinute"));
                            long questStart = Long.valueOf((String) q.get("scheduled")) + startTime.toMillisOfDay();
                            Long reminderStart = questStart + TimeUnit.MINUTES.toMillis(Long.valueOf((String) reminder.get("minutesFromStart")));
                            reminder.put("start", String.valueOf(reminderStart));
                        }
                    }
                    save(q);
                }
            }
            return true;
        });
    }

    private void saveProps(Map<String, List<Map<String, Object>>> documents, String rewards2) {
        if (documents.containsKey(rewards2)) {
            List<Map<String, Object>> rewards = documents.get(rewards2);
            for (Map<String, Object> reward : rewards) {
                save(reward);
            }
        }
    }

    private void showErrorMessage(Exception e) {
        eventBus.post(new AppErrorEvent(e));
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            finish();
        });
    }
    
    @Override
    public void onBackPressed() {

    }

    private void save(Map<String, Object> obj) {
        try {
            Document document = database.getDocument((String) obj.get("id"));
            document.putProperties(obj);
        } catch (CouchbaseLiteException e) {
            eventBus.post(new AppErrorEvent(e));
        }
    }

    private void onFinish() {
        startActivity(new Intent(MigrationActivity.this, MainActivity.class));
        finish();
    }

    private interface OnFinishListener{
        void onFinish();
    }
}