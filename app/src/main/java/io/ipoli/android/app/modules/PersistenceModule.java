package io.ipoli.android.app.modules;

import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.FirebaseChallengePersistenceService;
import io.ipoli.android.player.persistence.FirebasePlayerPersistenceService;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.persistence.FirebaseQuestPersistenceService;
import io.ipoli.android.quest.persistence.FirebaseRepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
@Module
public class PersistenceModule {

    @Provides
    @Singleton
    public QuestPersistenceService provideQuestPersistenceService(Context context, Bus eventBus) {
        return new FirebaseQuestPersistenceService(context, eventBus);
    }

    @Provides
    @Singleton
    public RepeatingQuestPersistenceService provideRepeatingQuestPersistenceService(Context context, Bus eventBus) {
        return new FirebaseRepeatingQuestPersistenceService(context, eventBus);
    }

    @Provides
    @Singleton
    public PlayerPersistenceService providePlayerPersistenceService(Context context, Bus eventBus) {
        return new FirebasePlayerPersistenceService(context, eventBus);
    }

    @Provides
    @Singleton
    public ChallengePersistenceService provideChallengePersistenceService(Context context, Bus eventBus) {
        return new FirebaseChallengePersistenceService(context, eventBus);
    }

}
