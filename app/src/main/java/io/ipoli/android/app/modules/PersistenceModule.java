package io.ipoli.android.app.modules;

import com.squareup.otto.Bus;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.player.persistence.RealmPlayerPersistenceService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmQuestPersistenceService;
import io.ipoli.android.quest.persistence.RealmRecurrentQuestPersistenceService;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class PersistenceModule {

    @Provides
    public QuestPersistenceService provideQuestPersistenceService(Bus eventBus) {
        return new RealmQuestPersistenceService(eventBus);
    }

    @Provides
    public RecurrentQuestPersistenceService provideRecurrentQuestPersistenceService(Bus eventBus) {
        return new RealmRecurrentQuestPersistenceService(eventBus);
    }

    @Provides
    public PlayerPersistenceService providePlayerPersistenceService() {
        return new RealmPlayerPersistenceService();
    }
}
