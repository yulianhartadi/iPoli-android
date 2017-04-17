package io.ipoli.android.app.modules;

import com.couchbase.lite.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.persistence.AndroidCalendarPersistenceService;
import io.ipoli.android.app.persistence.CalendarPersistenceService;
import io.ipoli.android.challenge.persistence.ChallengePersistenceService;
import io.ipoli.android.challenge.persistence.CouchbaseChallengePersistenceService;
import io.ipoli.android.player.persistence.CouchbasePlayerPersistenceService;
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
    public QuestPersistenceService provideQuestPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        return new CouchbaseQuestPersistenceService(database, objectMapper, eventBus);
    }

    @Provides
    public RepeatingQuestPersistenceService provideRepeatingQuestPersistenceService(Database database, ObjectMapper objectMapper, QuestPersistenceService questPersistenceService, Bus eventBus) {
        return new CouchbaseRepeatingQuestPersistenceService(database, objectMapper, questPersistenceService, eventBus);
    }

    @Provides
    public PlayerPersistenceService providePlayerPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        return new CouchbasePlayerPersistenceService(database, objectMapper, eventBus);
    }

    @Provides
    public ChallengePersistenceService provideChallengePersistenceService(Database database, ObjectMapper objectMapper, QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService, Bus eventBus) {
        return new CouchbaseChallengePersistenceService(database, objectMapper, questPersistenceService, repeatingQuestPersistenceService, eventBus);
    }

    @Provides
    public RewardPersistenceService provideRewardPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        return new CouchbaseRewardPersistenceService(database, objectMapper, eventBus);
    }

    @Provides
    public CalendarPersistenceService provideAndroidCalendarPersistenceService(Database database, PlayerPersistenceService playerPersistenceService, QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService, Bus eventBus){
        return new AndroidCalendarPersistenceService(database, playerPersistenceService, questPersistenceService, repeatingQuestPersistenceService, eventBus);
    }

}
