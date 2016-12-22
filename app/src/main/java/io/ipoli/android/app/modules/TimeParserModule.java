package io.ipoli.android.app.modules;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/22/16.
 */

@Module
public class TimeParserModule {

    @Provides
    @Singleton
    public PrettyTimeParser provideTimeParser() {
        return new PrettyTimeParser();
    }
}
