package io.ipoli.android.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;

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

        String firebasePlayerId = App.getPlayerId();
        api.migratePlayer(firebasePlayerId, new Api.PlayerMigratedListener() {
            @Override
            public void onSuccess(Map<String, List<Map<String, Object>>> documents) {
                database.runInTransaction(() -> {
                    Map<String, Object> player = documents.get("player").get(0);
                    Document document = database.createDocument();
                    String playerId = document.getId();
                    player.put("owner", playerId);
                    Log.d("AAAAA players Id", playerId);
                    try {
                        document.putProperties(player);
                    } catch (CouchbaseLiteException e) {
                        e.printStackTrace();
                    }
                    List<Map<String, Object>> rewards = documents.get("rewards");
                    for(Map<String, Object> reward : rewards) {
                        reward.put("owner", playerId);
                        save(reward);
                    }
                    eventBus.post(new PlayerCreatedEvent(playerId));
                    startActivity(new Intent(MigrationActivity.this, MainActivity.class));
                    finish();
                    return true;
                });
            }

            @Override
            public void onError(Exception e) {
                eventBus.post(new AppErrorEvent(e));
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    private void save(Map<String, Object> obj) {
        try {
            Document document = database.createDocument();
            document.putProperties(obj);
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
    }
}