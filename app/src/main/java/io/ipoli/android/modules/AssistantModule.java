package io.ipoli.android.modules;

import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.assistant.Assistant;
import io.ipoli.android.assistant.SimpleAssistant;
import io.ipoli.android.quest.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AssistantModule {

    @Provides
    @Singleton
    public Assistant provideAssistant(QuestPersistenceService questPersistenceService, Bus eventBus) {
        return new SimpleAssistant(questPersistenceService, eventBus);
    }

}
