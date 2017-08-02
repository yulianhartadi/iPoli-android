package io.ipoli.android.di

import android.content.Context
import dagger.Module
import dagger.Provides
import io.ipoli.android.player.PlayerRepository
import javax.inject.Singleton

/**
 * Created by vini on 8/1/17.
 */
@Module
class AppModule(val context: Context) {

    @Provides
    @Singleton
    fun provideContext(): Context = context
}