package io.ipoli.android.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.assistant.Assistant;
import io.ipoli.android.assistant.SimpleAssistant;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AssistantModule {

    @Provides
    @Singleton
    public Assistant provideAssistant() {
        return new SimpleAssistant();
    }

}
