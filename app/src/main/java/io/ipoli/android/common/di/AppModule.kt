package io.ipoli.android.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.persistence.RealmPlayerRepository
import javax.inject.Singleton

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
@Module
class AppModule(val context: Context) {

    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository = RealmPlayerRepository()
}