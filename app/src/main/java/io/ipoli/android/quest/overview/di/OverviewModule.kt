package io.ipoli.android.quest.overview.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.quest.overview.DisplayOverviewQuestsUseCase
import io.ipoli.android.quest.persistence.QuestRepository
import io.ipoli.android.quest.persistence.RealmQuestRepository

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
@Module
class OverviewModule {

    @Provides
    @OverviewScope
    fun provideQuestRepository(): QuestRepository = RealmQuestRepository()

    @Provides
    @OverviewScope
    fun provideDisplayOverviewQuestsUseCase(questRepository: QuestRepository): DisplayOverviewQuestsUseCase =
        DisplayOverviewQuestsUseCase(questRepository)
}