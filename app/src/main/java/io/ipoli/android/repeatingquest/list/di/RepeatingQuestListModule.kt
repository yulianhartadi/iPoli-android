package io.ipoli.android.repeatingquest.list.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.repeatingquest.list.usecase.DisplayRepeatingQuestListUseCase
import io.ipoli.android.repeatingquest.persistence.RealmRepeatingQuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/22/17.
 */
@Module
class RepeatingQuestListModule {

    @Provides
    @RepeatingQuestListScope
    fun provideRepeatingQuestRepository(): RepeatingQuestRepository =
        RealmRepeatingQuestRepository()

    @Provides
    @RepeatingQuestListScope
    fun provideDisplayRepeatingQuestListUseCase(repeatingQuestRepository: RepeatingQuestRepository): DisplayRepeatingQuestListUseCase =
        DisplayRepeatingQuestListUseCase(repeatingQuestRepository)
}