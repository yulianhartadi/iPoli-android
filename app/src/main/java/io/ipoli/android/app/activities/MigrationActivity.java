package io.ipoli.android.app.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.api.Api;
import io.ipoli.android.app.events.AppErrorEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/30/16.
 */

public class MigrationActivity extends BaseActivity {

    @Inject
    Api api;

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

        String firebasePlayerId = localStorage.readString(Constants.KEY_PLAYER_ID);
        firebasePlayerId = "-KRiVjXZpn3xHtTMKHGj";
        api.migratePlayer(firebasePlayerId, new Api.PlayerMigratedListener() {
            @Override
            public void onSuccess(Map<String, List<Object>> documents) {
                finish();
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
}
