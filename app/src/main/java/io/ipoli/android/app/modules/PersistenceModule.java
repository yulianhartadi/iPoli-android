package io.ipoli.android.app.modules;

import android.content.Context;

import com.squareup.otto.Bus;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.assistant.persistence.AssistantPersistenceService;
import io.ipoli.android.assistant.persistence.RealmAssistantPersistenceService;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.persistence.RealmPlayerPersistenceService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class PersistenceModule {

    @Provides
    public QuestPersistenceService provideQuestPersistenceService(Context context, Bus eventBus) {
        return new RealmQuestPersistenceService(context, eventBus);
    }

    @Provides
    public AssistantPersistenceService provideAssistantPersistenceService(Context context) {
        return new RealmAssistantPersistenceService(context);
    }

    @Provides
    public PlayerPersistenceService providePlayerPersistenceService(Context context) {
        return new RealmPlayerPersistenceService(context);
    }
}
