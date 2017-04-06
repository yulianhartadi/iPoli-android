package io.ipoli.android.app.modules;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.AndroidCalendarEventParser;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/6/17.
 */

@Module
public class AndroidCalendarParserModule {

    @Provides
    public AndroidCalendarEventParser providedAndroidCalendarEventParser(ExperienceRewardGenerator experienceRewardGenerator, CoinsRewardGenerator coinsRewardGenerator) {
        return new AndroidCalendarEventParser(experienceRewardGenerator, coinsRewardGenerator);
    }
}
