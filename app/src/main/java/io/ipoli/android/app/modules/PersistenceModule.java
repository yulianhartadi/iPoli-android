package io.ipoli.android.app.modules;

import com.google.gson.Gson;
import com.squareup.otto.Bus;

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
import io.ipoli.android.reward.persistence.FirebaseRewardPersistenceService;
import io.ipoli.android.reward.persistence.RewardPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
@Module
public class PersistenceModule {

    @Provides
    public QuestPersistenceService provideQuestPersistenceService(Bus eventBus, Gson gson) {
        return new FirebaseQuestPersistenceService(eventBus, gson);
    }

    @Provides
    public RepeatingQuestPersistenceService provideRepeatingQuestPersistenceService(Bus eventBus, Gson gson) {
        return new FirebaseRepeatingQuestPersistenceService(eventBus, gson);
    }

    @Provides
    public PlayerPersistenceService providePlayerPersistenceService(Bus eventBus, Gson gson) {
        return new FirebasePlayerPersistenceService(eventBus, gson);
    }

    @Provides
    public ChallengePersistenceService provideChallengePersistenceService(Bus eventBus, Gson gson) {
        return new FirebaseChallengePersistenceService(eventBus, gson);
    }

    @Provides
    public RewardPersistenceService provideRewardPersistenceService(Bus eventBus, Gson gson) {
        return new FirebaseRewardPersistenceService(eventBus, gson);
    }
}
