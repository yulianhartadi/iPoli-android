package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.app.services.readers.AndroidCalendarQuestListPersistenceService;
import io.ipoli.android.app.services.readers.AndroidCalendarRepeatingQuestListPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/29/16.
 */
@Module
public class AndroidCalendarPersistenceModule {

    @Provides
    @Singleton
    public AndroidCalendarQuestListPersistenceService provideQuestListReader() {
        return new AndroidCalendarQuestListPersistenceService();
    }

    @Provides
    @Singleton
    public AndroidCalendarRepeatingQuestListPersistenceService provideRepeatingQuestListReader() {
        return new AndroidCalendarRepeatingQuestListPersistenceService();
    }
}
