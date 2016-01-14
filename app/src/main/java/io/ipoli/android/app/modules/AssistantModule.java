package io.ipoli.android.app.modules;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.services.CommandParserService;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.assistant.SimpleAssistantService;
import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AssistantModule {

    @Provides
    @Singleton
    public AssistantService provideAssistant(AssistantPersistenceService assistantPersistenceService, QuestPersistenceService questPersistenceService, CommandParserService commandParserService, Bus eventBus, Context context) {
        return new SimpleAssistantService(assistantPersistenceService, questPersistenceService, commandParserService, eventBus, context);
    }

}
