package io.ipoli.android.app.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.UnsavedRevision;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.PlayerMigratedEvent;
import io.ipoli.android.app.sync.AndroidCalendarSyncJobService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.data.Avatar;
import io.ipoli.android.player.data.MembershipType;
import io.ipoli.android.player.data.PetAvatar;
import io.ipoli.android.player.data.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.scheduling.PowerUpScheduler;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.store.PowerUp;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class MigrationActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Boolean> {
    private static final int VERSION_BEFORE_UPGRADES = 3;
    private static final int NEW_CALENDAR_IMPORT_VERSION = 6;
    private static final int SUBSCRIPTIONS_FIRST_VERSION = 8;

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

    @Inject
    QuestPersistenceService questPersistenceService;

    @Inject
    RepeatingQuestPersistenceService repeatingQuestPersistenceService;

    private int schemaVersion;

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

        Player player = getPlayer();
        schemaVersion = player.getSchemaVersion();

        if (schemaVersion >= VERSION_BEFORE_UPGRADES) {
            getSupportLoaderManager().initLoader(1, null, this);
        } else {
            onFinish();
        }
    }

    private void showErrorMessageAndFinish() {
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            finish();
        });
    }

    @Override
    public void onBackPressed() {

    }

    private void onFinish() {
        eventBus.post(new PlayerMigratedEvent(schemaVersion, Constants.SCHEMA_VERSION));
        Player player = playerPersistenceService.get();
        player.setSchemaVersion(Constants.SCHEMA_VERSION);
        playerPersistenceService.save(player);
        PowerUpScheduler.scheduleExpirationCheckJob(getApplicationContext());
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new MigrationLoader(this, schemaVersion, getPlayerId(), playerPersistenceService,
                database, eventBus, questPersistenceService, repeatingQuestPersistenceService);
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        if (data) {
            onFinish();
        } else {
            showErrorMessageAndFinish();
        }
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
        //intentional
    }

    private static class MigrationException extends Exception {
        public MigrationException(String message, int schemaVersion) {
            super("Migration of player with id " + App.getPlayerId() + " and schema " + schemaVersion + " failed: " + message);
        }
    }

    private static class MigrationLoader extends AsyncTaskLoader<Boolean> {

        private int schemaVersion;
        private final String playerId;
        private PlayerPersistenceService playerPersistenceService;
        private Database database;
        private Bus eventBus;
        private QuestPersistenceService questPersistenceService;
        private RepeatingQuestPersistenceService repeatingQuestPersistenceService;

        public MigrationLoader(Context context, int schemaVersion, String playerId, PlayerPersistenceService playerPersistenceService,
                               Database database, Bus eventBus, QuestPersistenceService questPersistenceService,
                               RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
            super(context);
            this.schemaVersion = schemaVersion;
            this.playerId = playerId;
            this.playerPersistenceService = playerPersistenceService;
            this.database = database;
            this.eventBus = eventBus;
            this.questPersistenceService = questPersistenceService;
            this.repeatingQuestPersistenceService = repeatingQuestPersistenceService;
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }

        @Override
        public Boolean loadInBackground() {

            UnsavedRevision revision = database.getExistingDocument(playerId).createRevision();
            Map<String, Object> playerProperties = revision.getProperties();

            updateAvatars(playerProperties);

            if (schemaVersion < SUBSCRIPTIONS_FIRST_VERSION) {
                migrateRewardPoints(playerProperties);
                migrateUpgradesToPowerUps(playerProperties);
                removeRepeatingQuestAndUpdatePowerUps(playerProperties);
            }

            if (schemaVersion < Constants.PROFILES_FIRST_SCHEMA_VERSION) {
                migrateUsername(playerProperties);
            }

            revision.setProperties(playerProperties);
            try {
                revision.save();
            } catch (CouchbaseLiteException e) {
                eventBus.post(new AppErrorEvent(e));
            }

            if (schemaVersion < NEW_CALENDAR_IMPORT_VERSION) {
                return migrateAndroidCalendars();
            }
            return true;
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        private void removeRepeatingQuestAndUpdatePowerUps(Map<String, Object> playerProperties) {
            playerProperties.put("membershipType", MembershipType.NONE.name());

            if (!playerProperties.containsKey("inventory")) {
                return;
            }

            Map<String, Object> inventory = (Map<String, Object>) playerProperties.get("inventory");
            if (!inventory.containsKey("powerUps")) {
                inventory.put("powerUps", new HashMap<>());
            }

            Map<String, String> powerUps = (Map<String, String>) inventory.get("powerUps");
            if (schemaVersion == 3 || schemaVersion == 4) {

                for (PowerUp powerUp : PowerUp.values()) {
                    powerUps.put(String.valueOf(powerUp.code), "0");
                }
            }

            String repeatingQuestCode = "2";
            powerUps.remove(repeatingQuestCode);

            String after3Years = String.valueOf(DateUtils.toMillis(LocalDate.now().plusYears(3).minusDays(1)));
            String yesterday = String.valueOf(DateUtils.toMillis(LocalDate.now().minusDays(1)));

            for (PowerUp powerUp : PowerUp.values()) {
                String powerUpCode = String.valueOf(powerUp.code);
                if (powerUps.containsKey(powerUpCode)) {
                    powerUps.put(powerUpCode, after3Years);
                } else {
                    powerUps.put(powerUpCode, yesterday);
                }
            }
        }

        private void migrateUpgradesToPowerUps(Map<String, Object> playerProperties) {
            if (!playerProperties.containsKey("inventory")) {
                return;
            }

            Map<String, Object> inventory = (Map<String, Object>) playerProperties.get("inventory");
            if (!inventory.containsKey("upgrades")) {
                return;
            }

            Map<String, String> powerUps = new HashMap<>((Map<String, String>) inventory.get("upgrades"));
            inventory.remove("upgrades");

            inventory.put("powerUps", powerUps);
        }

        private void updateAvatars(Map<String, Object> playerProperties) {
            String playerPicture = null;
            String petPicture = null;

            if (playerProperties.containsKey("picture")) {
                playerPicture = (String) playerProperties.get("picture");
                playerProperties.remove("picture");
            }
            List<Map<String, Object>> currentPlayerPets = (List<Map<String, Object>>) playerProperties.get("pets");
            Map<String, Object> pet = currentPlayerPets.get(0);
            if (pet.containsKey("picture")) {
                petPicture = (String) pet.remove("picture");
            }


            if (!playerProperties.containsKey("inventory")) {
                playerProperties.put("inventory", new HashMap<>());
            }

            Map<String, Object> inventory = (Map<String, Object>) playerProperties.get("inventory");
            String unlockedDate = (String) playerProperties.get("createdAt");

            if (playerPicture != null) {
                Avatar playerAvatar = Constants.DEFAULT_PLAYER_AVATAR;
                for (Avatar avatar : Avatar.values()) {
                    String avatarPicture = getContext().getResources().getResourceEntryName(avatar.picture);
                    if (avatarPicture.equals(playerPicture)) {
                        playerAvatar = avatar;
                        break;
                    }
                }

                Map<String, String> avatars = new HashMap<>();
                avatars.put(String.valueOf(playerAvatar.code), unlockedDate);
                inventory.put("avatars", avatars);
                playerProperties.put("avatarCode", playerAvatar.code);
            } else if (!playerProperties.containsKey("avatarCode")) {
                Map<String, String> avatars = new HashMap<>();
                avatars.put(String.valueOf(Constants.DEFAULT_PLAYER_AVATAR.code), unlockedDate);
                inventory.put("avatars", avatars);
                playerProperties.put("avatarCode", String.valueOf(Constants.DEFAULT_PLAYER_AVATAR.code));
            }

            if (petPicture != null) {
                PetAvatar playerPetAvatar = Constants.DEFAULT_PET_AVATAR;
                for (PetAvatar petAvatar : PetAvatar.values()) {
                    String petAvatarPicture = getContext().getResources().getResourceEntryName(petAvatar.picture);
                    if (petAvatarPicture.equals(petPicture)) {
                        playerPetAvatar = petAvatar;
                        break;
                    }
                }
                Map<String, String> pets = new HashMap<>();
                pets.put(String.valueOf(playerPetAvatar.code), unlockedDate);
                inventory.put("pets", pets);
                pet.put("avatarCode", String.valueOf(playerPetAvatar.code));
            } else if (!pet.containsKey("avatarCode")) {
                Map<String, String> pets = new HashMap<>();
                pets.put(String.valueOf(Constants.DEFAULT_PET_AVATAR.code), unlockedDate);
                inventory.put("pets", pets);
                pet.put("avatarCode", String.valueOf(Constants.DEFAULT_PET_AVATAR.code));
            }
        }

        private void migrateRewardPoints(Map<String, Object> playerProperties) {

            if (playerProperties.containsKey("rewardPoints")) {
                Long rewardPoints = Long.valueOf((String) playerProperties.get("rewardPoints"));
                playerProperties.remove("rewardPoints");

                Long currentCoins = Long.valueOf((String) playerProperties.get("coins"));
                playerProperties.put("coins", String.valueOf(currentCoins + Math.min(500, rewardPoints)));
            }
        }

        private void migrateUsername(Map<String, Object> playerProperties) {
            playerProperties.put("displayName", playerProperties.get("username"));
            playerProperties.put("username", "");
        }

        private boolean migrateAndroidCalendars() {
            //delete old not completed from android calendar
            Player player = playerPersistenceService.get();

            if (player.getAndroidCalendars().isEmpty()) {
                return true;
            }

            List<Quest> questsToDelete = new ArrayList<>();
            List<RepeatingQuest> repeatingQuestsToDelete = new ArrayList<>();

            for (Long calendarId : player.getAndroidCalendars().keySet()) {
                questsToDelete.addAll(questPersistenceService.findNotCompletedFromAndroidCalendar(calendarId));
                repeatingQuestsToDelete.addAll(repeatingQuestPersistenceService.findNotCompletedFromAndroidCalendar(calendarId));
            }

            boolean deleteSuccessfully = deleteOldCalendarQuests(questsToDelete, repeatingQuestsToDelete);

            if (!deleteSuccessfully) {
                eventBus.post(new AppErrorEvent(new MigrationException("could not delete existing google calendar quests", schemaVersion)));
                return false;
            }


            JobScheduler jobScheduler = (JobScheduler) getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = new JobInfo.Builder(1,
                    new ComponentName(getContext(), AndroidCalendarSyncJobService.class))
                    .setOverrideDeadline(0)
                    .build();
            jobScheduler.schedule(jobInfo);

            return true;
        }

        private boolean deleteOldCalendarQuests(List<Quest> questsToDelete, List<RepeatingQuest> repeatingQuestsToDelete) {
            return database.runInTransaction(() -> {
                for (Quest q : questsToDelete) {
                    if (!delete(q.getId())) {
                        return false;
                    }
                }

                for (RepeatingQuest rq : repeatingQuestsToDelete) {
                    List<Quest> quests = questPersistenceService.findAllForRepeatingQuest(rq.getId());
                    for (Quest q : quests) {
                        if (q.isCompleted()) {
                            q.setRepeatingQuestId(null);
                            questPersistenceService.save(q);
                        } else {
                            if (!delete(q.getId())) {
                                return false;
                            }
                        }
                    }
                    if (!delete(rq.getId())) {
                        return false;
                    }
                }
                return true;
            });
        }

        private boolean delete(String id) {
            try {
                database.getExistingDocument(id).delete();
            } catch (CouchbaseLiteException e) {
                eventBus.post(new AppErrorEvent(e));
                return false;
            }
            return true;
        }
    }
}