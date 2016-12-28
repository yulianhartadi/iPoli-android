package io.ipoli.android.app.modules;

import com.squareup.otto.Bus;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.avatar.persistence.FirebaseAvatarPersistenceService;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.FirebaseChallengePersistenceService;
import io.ipoli.android.pet.persistence.FirebasePetPersistenceService;
import io.ipoli.android.pet.persistence.PetPersistenceService;
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
    public QuestPersistenceService provideQuestPersistenceService(Bus eventBus) {
        return new FirebaseQuestPersistenceService(eventBus);
    }

    @Provides
    public RepeatingQuestPersistenceService provideRepeatingQuestPersistenceService(Bus eventBus, QuestPersistenceService questPersistenceService) {
        return new FirebaseRepeatingQuestPersistenceService(eventBus, questPersistenceService);
    }

    @Provides
    public PlayerPersistenceService providePlayerPersistenceService(Bus eventBus) {
        return new FirebasePlayerPersistenceService(eventBus);
    }

    @Provides
    public AvatarPersistenceService provideAvatarPersistenceService(Bus eventBus) {
        return new FirebaseAvatarPersistenceService(eventBus);
    }

    @Provides
    public PetPersistenceService providePetPersistenceService(Bus eventBus, LocalStorage localStorage) {
        return new FirebasePetPersistenceService(eventBus, localStorage);
    }

    @Provides
    public ChallengePersistenceService provideChallengePersistenceService(Bus eventBus) {
        return new FirebaseChallengePersistenceService(eventBus);
    }

    @Provides
    public RewardPersistenceService provideRewardPersistenceService(Bus eventBus) {
        return new FirebaseRewardPersistenceService(eventBus);
    }
}
