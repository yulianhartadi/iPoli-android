package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.NetworkConnectionChangedEvent;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/16.
 */
public class SignInActivity extends BaseActivity {

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(this).inject(this);
        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(this, task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Cannot create player, please try again.", Toast.LENGTH_LONG).show();
                return;
            }
            String uid = task.getResult().getUser().getUid();
            Player player = new Player(uid, String.valueOf(Constants.DEFAULT_PLAYER_XP), Constants.DEFAULT_PLAYER_LEVEL, Constants.DEFAULT_PLAYER_COINS, Constants.DEFAULT_PLAYER_AVATAR);
            playerPersistenceService.save(player, () -> {
                localStorage.saveString(Constants.KEY_PLAYER_ID, player.getId());
                eventBus.post(new PlayerCreatedEvent(player.getId()));
                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                finish();
            });

        });
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }


    @Subscribe
    public void onNetworkChanged(NetworkConnectionChangedEvent e) {
        if(!e.hasInternet) {
            showNoInternetActivity();
        }
    }
}
