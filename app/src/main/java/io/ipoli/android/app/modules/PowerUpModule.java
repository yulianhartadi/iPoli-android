package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.player.PowerUpManager;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 5/22/17.
 */

@Module
public class PowerUpModule {

    @Provides
    @Singleton
    public PowerUpManager providePowerUpManager(PlayerPersistenceService playerPersistenceService) {
        return new PowerUpManager(playerPersistenceService);
    }
}