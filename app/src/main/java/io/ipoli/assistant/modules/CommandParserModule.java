package io.ipoli.assistant.modules;

import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.assistant.services.CommandParserService;
import io.ipoli.assistant.services.LocalCommandParserService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class CommandParserModule {

    @Provides
    @Singleton
    public CommandParserService provideCommandParserService(Bus bus) {
        return new LocalCommandParserService(bus);
    }
}
