package io.ipoli.android.player.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.app.App;
import io.ipoli.android.app.activities.BaseActivity;
import io.ipoli.android.app.events.PlayerCreatedEvent;
import io.ipoli.android.avatar.Avatar;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.pet.data.Pet;
import io.ipoli.android.pet.persistence.PetPersistenceService;
import io.ipoli.android.player.Player;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/16.
 */
public class SignInActivity extends BaseActivity {

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Inject
    AvatarPersistenceService avatarPersistenceService;

    @Inject
    PetPersistenceService petPersistenceService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.getAppComponent(this).inject(this);

        Pet pet = new Pet(Constants.DEFAULT_PET_NAME, Constants.DEFAULT_PET_AVATAR, Constants.DEFAULT_PET_BACKGROUND_IMAGE, Constants.DEFAULT_PET_HP);
        Avatar avatar = new Avatar(String.valueOf(Constants.DEFAULT_PLAYER_XP), Constants.DEFAULT_AVATAR_LEVEL, Constants.DEFAULT_PLAYER_COINS, Constants.DEFAULT_PLAYER_PICTURE);
        Player player = new Player(pet, avatar);
        playerPersistenceService.save(player);
        localStorage.saveString(Constants.KEY_PLAYER_ID, player.getId());
        localStorage.saveInt(Constants.KEY_XP_BONUS_PERCENTAGE, pet.getExperienceBonusPercentage());
        localStorage.saveInt(Constants.KEY_COINS_BONUS_PERCENTAGE, pet.getCoinsBonusPercentage());
        eventBus.post(new PlayerCreatedEvent(player.getId()));
        startActivity(new Intent(this, MainActivity.class));
        finish();
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
}
