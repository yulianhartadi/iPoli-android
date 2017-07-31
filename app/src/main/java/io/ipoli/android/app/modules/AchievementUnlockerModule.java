package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.achievement.AchievementUnlocker;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
@Module
public class AchievementUnlockerModule {

    @Provides
    @Singleton
    public AchievementUnlocker provideAchievementUnlocker() {
        return new AchievementUnlocker();
    }
}
