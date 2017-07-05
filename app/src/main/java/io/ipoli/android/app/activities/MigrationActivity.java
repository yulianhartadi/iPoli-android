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
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.UnsavedRevision;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
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
import io.ipoli.android.app.sync.AndroidCalendarSyncJobService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.player.Avatar;
import io.ipoli.android.player.PetAvatar;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.store.Upgrade;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class MigrationActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Boolean> {
    private static final int VERSION_BEFORE_UPGRADES = 3;
    private static final int NEW_CALENDAR_IMPORT_VERSION = 6;

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

        if (schemaVersion >= VERSION_BEFORE_UPGRADES) {//leave only == when null pointers for rewardPoints and avatars are fixed
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
        Player player = playerPersistenceService.get();
        player.setSchemaVersion(Constants.SCHEMA_VERSION);
        playerPersistenceService.save(player);
        startActivity(new Intent(MigrationActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new MigrationLoader(this, schemaVersion, playerPersistenceService,
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

    private interface OnMigrationFinishListener {
        void onMigrationFinish();
    }

    private static class MigrationException extends Exception {
        public MigrationException(String message, int schemaVersion) {
            super("Migration of player with id " + App.getPlayerId() + " and schema " + schemaVersion + " failed: " + message);
        }
    }

    private static class MigrationLoader extends AsyncTaskLoader<Boolean> {

        private int schemaVersion;
        private PlayerPersistenceService playerPersistenceService;
        private Database database;
        private Bus eventBus;
        private QuestPersistenceService questPersistenceService;
        private RepeatingQuestPersistenceService repeatingQuestPersistenceService;

        public MigrationLoader(Context context, int schemaVersion, PlayerPersistenceService playerPersistenceService,
                               Database database, Bus eventBus, QuestPersistenceService questPersistenceService,
                               RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
            super(context);
            this.schemaVersion = schemaVersion;
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
            unlockUpgrades();
            if(schemaVersion < Constants.PROFILES_FIRST_SCHEMA_VERSION) {
                migrateUsername();
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

        private void migrateUsername() {
            Player player = playerPersistenceService.get();
            player.setDisplayName(player.getUsername());
            player.setUsername("");
            playerPersistenceService.save(player);
        }


        private void unlockUpgrades() {
            Player player = playerPersistenceService.get();

            Pair<String, String> pics = updatePictures(player.getId());

            String playerPicture = pics.first;
            String petPicture = pics.second;

            player = playerPersistenceService.get();
            LocalDate unlockedDate = DateUtils.fromMillis(player.getCreatedAt());
            if (schemaVersion == 3 || schemaVersion == 4) {
                for (Upgrade upgrade : Upgrade.values()) {
                    player.getInventory().addUpgrade(upgrade, unlockedDate);
                }
            }

            if (player.getRewardPoints() == player.getCoins()) {
                player.setRewardPoints(player.getCoins());
            }

            if (playerPicture != null) {
                Avatar playerAvatar = Constants.DEFAULT_PLAYER_AVATAR;
                for (Avatar avatar : Avatar.values()) {
                    String avatarPicture = getContext().getResources().getResourceEntryName(avatar.picture);
                    if (avatarPicture.equals(playerPicture)) {
                        playerAvatar = avatar;
                        break;
                    }
                }
                player.getInventory().addAvatar(playerAvatar, unlockedDate);
                player.setAvatar(playerAvatar);
            } else if (player.getAvatarCode() == null) {
                player.setAvatar(Constants.DEFAULT_PLAYER_AVATAR);
                player.getInventory().addAvatar(Constants.DEFAULT_PLAYER_AVATAR, unlockedDate);
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
                player.getInventory().addPet(playerPetAvatar, unlockedDate);
                player.getPet().setPetAvatar(playerPetAvatar);
            } else if (player.getPet().getAvatarCode() == null) {
                player.getInventory().addPet(Constants.DEFAULT_PET_AVATAR, unlockedDate);
                player.getPet().setPetAvatar(Constants.DEFAULT_PET_AVATAR);
            }

            playerPersistenceService.save(player);
        }

        private Pair<String, String> updatePictures(String playerId) {
            String playerPicture = null;
            String petPicture = null;

            UnsavedRevision revision = database.getExistingDocument(playerId).createRevision();
            Map<String, Object> properties = revision.getProperties();
            if (properties.containsKey("picture")) {
                playerPicture = (String) properties.get("picture");
                properties.remove("picture");
            }
            List<Map<String, Object>> pets = (List<Map<String, Object>>) properties.get("pets");
            Map<String, Object> pet = pets.get(0);
            if (pet.containsKey("picture")) {
                petPicture = (String) pet.remove("picture");
            }

            if (playerPicture == null && petPicture == null) {
                return new Pair<>(null, null);
            }

            revision.setProperties(properties);
            try {
                revision.save();
            } catch (CouchbaseLiteException e) {
                eventBus.post(new AppErrorEvent(e));
            }

            return new Pair<>(playerPicture, petPicture);
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