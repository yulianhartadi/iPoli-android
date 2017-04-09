package io.ipoli.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.squareup.otto.Bus;

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
import io.ipoli.android.app.utils.NetworkConnectivityUtils;
import io.ipoli.android.app.utils.Time;

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
                database.runInTransaction(() -> {
                    player.put("schemaVersion", Constants.SCHEMA_VERSION);
                    save(player);

                    if (documents.containsKey("rewards")) {
                        List<Map<String, Object>> rewards = documents.get("rewards");
                        for (Map<String, Object> reward : rewards) {
                            save(reward);
                        }
                    }

                    if (documents.containsKey("challenges")) {
                        List<Map<String, Object>> challenges = documents.get("challenges");
                        for (Map<String, Object> challenge : challenges) {
                            save(challenge);
                        }
                    }

                    if (documents.containsKey("repeating_quests")) {
                        List<Map<String, Object>> repeatingQuests = documents.get("repeating_quests");
                        for (Map<String, Object> rq : repeatingQuests) {
                            save(rq);
                        }
                    }

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
                String playerId = (String) player.get("id");
                eventBus.post(new PlayerCreatedEvent(playerId));
                eventBus.post(new PlayerMigratedEvent(firebasePlayerId, playerId));
                startActivity(new Intent(MigrationActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                showErrorMessage(e);
            }
        });
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
}