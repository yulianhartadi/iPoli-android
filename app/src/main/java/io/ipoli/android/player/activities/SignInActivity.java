package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.pet.data.Pet;
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

        Pet pet = new Pet(Constants.DEFAULT_PET_NAME, Constants.DEFAULT_PET_AVATAR, Constants.DEFAULT_PET_BACKGROUND_IMAGE, Constants.DEFAULT_PET_HP);
        Player player = new Player(String.valueOf(Constants.DEFAULT_PLAYER_XP),
                Constants.DEFAULT_AVATAR_LEVEL,
                Constants.DEFAULT_PLAYER_COINS,
                Constants.DEFAULT_PLAYER_PICTURE,
                DateFormat.is24HourFormat(this), pet);
        playerPersistenceService.save(player);
        eventBus.post(new PlayerCreatedEvent(player.getId()));
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
