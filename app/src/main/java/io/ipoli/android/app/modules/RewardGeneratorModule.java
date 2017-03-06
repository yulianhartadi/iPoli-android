package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/24/16.
 */
@Module
public class RewardGeneratorModule {

    @Provides
    @Singleton
    public ExperienceRewardGenerator provideExperienceRewardGenerator(PlayerPersistenceService playerPersistenceService) {
        return new ExperienceRewardGenerator(playerPersistenceService);
    }

    @Provides
    @Singleton
    public CoinsRewardGenerator provideCoinsRewardGenerator(PlayerPersistenceService playerPersistenceService) {
        return new CoinsRewardGenerator(playerPersistenceService);
    }
}
