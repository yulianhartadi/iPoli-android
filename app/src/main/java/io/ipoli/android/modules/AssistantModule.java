package io.ipoli.android.modules;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.assistant.SimpleAssistantService;
import io.ipoli.android.quest.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AssistantModule {

    @Provides
    @Singleton
    public AssistantService provideAssistant(QuestPersistenceService questPersistenceService, Bus eventBus) {
        return new SimpleAssistantService(questPersistenceService, eventBus);
    }

}
