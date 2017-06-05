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
import com.couchbase.lite.Document;
import com.couchbase.lite.UnsavedRevision;
import com.squareup.otto.Bus;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
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
import io.ipoli.android.app.sync.AndroidCalendarSyncJobService;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.Time;
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
    public static final int VERSION_BEFORE_UPGRADES = 3;
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

        schemaVersion = localStorage.readInt(Constants.KEY_SCHEMA_VERSION);

        if (schemaVersion < VERSION_BEFORE_UPGRADES) {
            migrateFromFirebase(() -> {
                getSupportLoaderManager().initLoader(1, null, this);
            });
        } else if (schemaVersion >= VERSION_BEFORE_UPGRADES) {//leave only == when null pointers for rewardPoints and avatars are fixed
            getSupportLoaderManager().initLoader(1, null, this);
        } else {
            onFinish();
        }
    }

    private void migrateFromFirebase(OnMigrationFinishListener migrationFinishListener) {
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
                    showErrorMessageAndFinish(new MigrationException("Player with firebase id:" + firebasePlayerId + " not found", schemaVersion));
                    return;
                }
                Map<String, Object> player = documents.get("player").get(0);
                saveDocuments(documents, player);
                String playerId = (String) player.get("id");
                eventBus.post(new PlayerCreatedEvent(playerId));
                eventBus.post(new PlayerMigratedEvent(firebasePlayerId, playerId));
                migrationFinishListener.onMigrationFinish();
            }

            @Override
            public void onError(Exception e) {
                showErrorMessageAndFinish(e);
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

    private void showErrorMessageAndFinish(Exception e) {
        eventBus.post(new AppErrorEvent(e));
        runOnUiThread(() -> {
            Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_LONG).show();
            finish();
        });
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

    private void save(Map<String, Object> obj) {
        try {
            Document document = database.getDocument((String) obj.get("id"));
            document.putProperties(obj);
        } catch (CouchbaseLiteException e) {
            eventBus.post(new AppErrorEvent(e));
        }
    }

    private void onFinish() {
        Player player = playerPersistenceService.get();
        player.setSchemaVersion(Constants.SCHEMA_VERSION);
        playerPersistenceService.save(player);
        localStorage.saveInt(Constants.KEY_SCHEMA_VERSION, Constants.SCHEMA_VERSION);
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
            if (schemaVersion < NEW_CALENDAR_IMPORT_VERSION) {
                return migrateAndroidCalendars();
            }
            return true;
        }

        @Override
        protected void onStopLoading() {
            cancelLoad();
        }

        private void unlockUpgrades() {
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
            if (schemaVersion == 3 || schemaVersion == 4) {
                for (Upgrade upgrade : Upgrade.values()) {
                    player.getInventory().addUpgrade(upgrade, unlockedDate);
                }
            }

            player.setRewardPoints(player.getCoins());

            for (Avatar avatar : Avatar.values()) {
                String avatarPicture = getContext().getResources().getResourceEntryName(avatar.picture);
                if (avatarPicture.equals(playerPicture)) {
                    player.getInventory().addAvatar(avatar, unlockedDate);
                    player.setAvatar(avatar);
                    break;
                }
            }

            for (PetAvatar petAvatar : PetAvatar.values()) {
                String petAvatarPicture = getContext().getResources().getResourceEntryName(petAvatar.picture);
                if (petAvatarPicture.equals(petPicture)) {
                    player.getInventory().addPet(petAvatar, unlockedDate);
                    player.getPet().setPetAvatar(petAvatar);
                }
            }

            playerPersistenceService.save(player);
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

            boolean deleteSuccessfully = database.runInTransaction(() -> {
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