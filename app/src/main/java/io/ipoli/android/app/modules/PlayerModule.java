package io.ipoli.android.app.modules;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.player.PlayerService;
import io.ipoli.android.player.SimplePlayerService;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/25/16.
 */
@Module
public class PlayerModule {

    @Provides
    @Singleton
    public PlayerService providePlayerService(Bus eventBus, PlayerPersistenceService playerPersistenceService) {
        return new SimplePlayerService(eventBus, playerPersistenceService);
    }
}
