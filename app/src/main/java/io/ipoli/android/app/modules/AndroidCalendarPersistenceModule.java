package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListPersistenceService;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListPersistenceService;
import io.ipoli.android.quest.persistence.QuestPersistenceService;
import io.ipoli.android.quest.persistence.RepeatingQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/29/16.
 */
@Module
public class AndroidCalendarPersistenceModule {

    @Provides
    @Singleton
    public AndroidCalendarQuestListPersistenceService provideQuestListReader(QuestPersistenceService questPersistenceService, RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        return new AndroidCalendarQuestListPersistenceService(questPersistenceService, repeatingQuestPersistenceService);
    }

    @Provides
    @Singleton
    public AndroidCalendarRepeatingQuestListPersistenceService provideRepeatingQuestListReader(RepeatingQuestPersistenceService repeatingQuestPersistenceService) {
        return new AndroidCalendarRepeatingQuestListPersistenceService(repeatingQuestPersistenceService);
    }
}
