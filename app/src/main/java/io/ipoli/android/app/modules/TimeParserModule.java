package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.parsers.DateTimeParser;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/22/16.
 */
@Module
public class TimeParserModule {

    @Provides
    @Singleton
    public DateTimeParser provideTimeParser() {
        return new DateTimeParser();
    }
}
