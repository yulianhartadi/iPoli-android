package io.ipoli.android.app.modules;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.sync.AndroidCalendarEventParser;
import io.ipoli.android.app.sync.SyncAndroidCalendarProvider;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.generators.RewardPointsRewardGenerator;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/6/17.
 */

@Module
public class AndroidCalendarModule {

    @Provides
    @Singleton
    public AndroidCalendarEventParser providedAndroidCalendarEventParser(SyncAndroidCalendarProvider syncAndroidCalendarProvider, Bus eventBus,
                                                                         CoinsRewardGenerator coinsRewardGenerator,
                                                                         ExperienceRewardGenerator experienceRewardGenerator,
                                                                         RewardPointsRewardGenerator rewardPointsRewardGenerator) {
        return new AndroidCalendarEventParser(syncAndroidCalendarProvider, eventBus, coinsRewardGenerator, experienceRewardGenerator, rewardPointsRewardGenerator);
    }

    @Provides
    @Singleton
    public SyncAndroidCalendarProvider provideSyncAndroidCalendarProvider(Context context, Bus eventBus) {
        return new SyncAndroidCalendarProvider(context, eventBus);
    }
}
