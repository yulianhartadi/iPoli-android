package io.ipoli.android.store.avatars

import android.content.Context
import dagger.Module
import dagger.Provides
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.persistence.RealmPlayerRepository
import io.ipoli.android.quest.overview.di.OverviewScope
import io.ipoli.android.quest.persistence.QuestRepository
import io.ipoli.android.quest.persistence.RealmQuestRepository
import io.ipoli.android.store.DisplayCoinsUseCase
import io.ipoli.android.store.StoreScope

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 8/20/17.
 */
@Module
class AvatarListModule {
    @Provides
    @AvatarListScope
    fun providePlayerRepository(): PlayerRepository = RealmPlayerRepository()

    @Provides
    @AvatarListScope
    fun provideDisplayRewardListUseCase(playerRepository: PlayerRepository): DisplayAvatarListUseCase = DisplayAvatarListUseCase(playerRepository)

    @Provides
    @AvatarListScope
    fun provideBuyAvatarUseCase(playerRepository: PlayerRepository): BuyAvatarUseCase = BuyAvatarUseCase(playerRepository)

    @Provides
    @AvatarListScope
    fun provideUseAvatarUseCase(playerRepository: PlayerRepository): UseAvatarUseCase = UseAvatarUseCase(playerRepository)
}