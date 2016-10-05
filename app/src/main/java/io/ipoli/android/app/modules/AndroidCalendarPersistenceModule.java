package io.ipoli.android.app.modules;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListPersistenceService;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListPersistenceService;
import io.ipoli.android.quest.generators.CoinsRewardGenerator;
import io.ipoli.android.quest.generators.ExperienceRewardGenerator;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/29/16.
 */
@Module
public class AndroidCalendarPersistenceModule {

    @Provides
    public AndroidCalendarQuestListPersistenceService provideQuestListReader(QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService,
                                                                             ExperienceRewardGenerator experienceRewardGenerator, CoinsRewardGenerator coinsRewardGenerator) {
        return new AndroidCalendarQuestListPersistenceService(questPersistenceService, repeatingQuestPersistenceService, experienceRewardGenerator, coinsRewardGenerator);
    }

    @Provides
    public AndroidCalendarRepeatingQuestListPersistenceService provideRepeatingQuestListReader(RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        return new AndroidCalendarRepeatingQuestListPersistenceService(repeatingQuestPersistenceService);
    }
}
