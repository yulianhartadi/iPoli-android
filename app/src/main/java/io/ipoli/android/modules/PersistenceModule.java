package io.ipoli.android.modules;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.quest.QuestPersistenceService;
import io.ipoli.android.quest.RealmQuestPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
@Module
public class PersistenceModule {

    @Provides
    @Singleton
    public QuestPersistenceService provideQuestPersistenceService(Context context) {
        return new RealmQuestPersistenceService(context);
    }
}
