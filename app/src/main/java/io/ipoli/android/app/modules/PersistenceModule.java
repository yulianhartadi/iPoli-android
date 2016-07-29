package io.ipoli.android.app.modules;

import android.content.Context;

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
    public QuestPersistenceService provideQuestPersistenceService(Context context, Bus eventBus) {
        return new FirebaseQuestPersistenceService(context, eventBus);
    }

    @Provides
    public RepeatingQuestPersistenceService provideRepeatingQuestPersistenceService(Context context, Bus eventBus) {
        return new FirebaseRepeatingQuestPersistenceService(context, eventBus);
    }

    @Provides
    public PlayerPersistenceService providePlayerPersistenceService(Context context, Bus eventBus) {
        return new FirebasePlayerPersistenceService(context, eventBus);
    }

    @Provides
    public ChallengePersistenceService provideChallengePersistenceService(Context context, Bus eventBus) {
        return new FirebaseChallengePersistenceService(context, eventBus);
    }

    @Provides
    public RewardPersistenceService provideRewardPersistenceService(Context context, Bus eventBus) {
        return new FirebaseRewardPersistenceService(context, eventBus);
    }
}
