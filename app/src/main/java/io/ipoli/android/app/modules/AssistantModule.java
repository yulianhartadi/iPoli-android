package io.ipoli.android.app.modules;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.assistant.AssistantService;
import io.ipoli.android.assistant.SimpleAssistantService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class AssistantModule {

    @Provides
    @Singleton
    public AssistantService provideAssistant(QuestPersistenceService questPersistenceService, Bus eventBus, Context context) {
        return new SimpleAssistantService(questPersistenceService, eventBus, context);
    }

}
