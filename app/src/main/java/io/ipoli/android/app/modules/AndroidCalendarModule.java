package io.ipoli.android.app.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.app.SyncAndroidCalendarProvider;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/6/17.
 */

@Module
public class AndroidCalendarModule {

    @Provides
    @Singleton
    public AndroidCalendarEventParser providedAndroidCalendarEventParser(ExperienceRewardGenerator experienceRewardGenerator, CoinsRewardGenerator coinsRewardGenerator) {
        return new AndroidCalendarEventParser(experienceRewardGenerator, coinsRewardGenerator);
    }

    @Provides
    @Singleton
    public SyncAndroidCalendarProvider provideSyncAndroidCalendarProvider(Context context) {
        return new SyncAndroidCalendarProvider(context);
    }
}
