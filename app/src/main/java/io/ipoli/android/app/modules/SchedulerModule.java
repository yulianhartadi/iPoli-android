package io.ipoli.android.app.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ipoli.android.quest.schedulers.QuestScheduler;
import io.ipoli.android.quest.schedulers.RepeatingQuestScheduler;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/8/16.
 */
@Module
public class SchedulerModule {

    @Provides
    @Singleton
    public RepeatingQuestScheduler provideRepeatingQuestScheduler() {
        return new RepeatingQuestScheduler();
    }

    @Provides
    @Singleton
    public QuestScheduler provideQuestScheduler() {
        return new QuestScheduler();
    }
}
