package io.ipoli.android.app.modules;

import com.couchbase.lite.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.avatar.persistence.AvatarPersistenceService;
import io.ipoli.android.avatar.persistence.FirebaseAvatarPersistenceService;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.CouchbaseChallengePersistenceService;
import io.ipoli.android.pet.persistence.CouchbasePetPersistenceService;
import io.ipoli.android.pet.persistence.PetPersistenceService;
import io.ipoli.android.player.persistence.FirebasePlayerPersistenceService;
import io.ipoli.android.player.persistence.PlayerPersistenceService;
import io.ipoli.android.quest.persistence.CouchbaseQuestPersistenceService;
import io.ipoli.android.quest.persistence.CouchbaseRepeatingQuestPersistenceService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;
import io.ipoli.android.reward.persistence.CouchbaseRewardPersistenceService;
import io.ipoli.android.reward.persistence.RewardPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
@Module
public class PersistenceModule {

    @Provides
    public QuestPersistenceService provideQuestPersistenceService(Database database, ObjectMapper objectMapper) {
        return new CouchbaseQuestPersistenceService(database, objectMapper);
    }

    @Provides
    public RepeatingQuestPersistenceService provideRepeatingQuestPersistenceService(Database database, ObjectMapper objectMapper, QuestPersistenceService questPersistenceService) {
        return new CouchbaseRepeatingQuestPersistenceService(database, objectMapper, questPersistenceService);
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
    public PetPersistenceService providePetPersistenceService(Database database, ObjectMapper objectMapper, LocalStorage localStorage) {
        return new CouchbasePetPersistenceService(database, objectMapper, localStorage);
    }

    @Provides
    public ChallengePersistenceService provideChallengePersistenceService(Database database, ObjectMapper objectMapper) {
        return new CouchbaseChallengePersistenceService(database, objectMapper);
    }

    @Provides
    public RewardPersistenceService provideRewardPersistenceService(Database database, ObjectMapper objectMapper) {
        return new CouchbaseRewardPersistenceService(database, objectMapper);
    }
}
