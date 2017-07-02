package io.ipoli.android.app.modules;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.feed.persistence.FeedPersistenceService;
import io.ipoli.android.player.PlayerCredentialsHandler;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/2/17.
 */
@Module
public class PlayerCredentialsModule {

    @Provides
    public PlayerCredentialsHandler providePlayerCredentialHandler(PlayerPersistenceService playerPersistenceService, FeedPersistenceService feedPersistenceService) {
        return new PlayerCredentialsHandler(playerPersistenceService, feedPersistenceService);
    }
}
