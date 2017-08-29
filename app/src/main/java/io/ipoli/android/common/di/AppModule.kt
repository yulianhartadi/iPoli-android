package io.ipoli.android.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import io.ipoli.android.common.jobservice.JobQueue
import io.ipoli.android.player.di.SignInScope
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.player.persistence.RealmPlayerRepository
import javax.inject.Singleton

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/17.
 */
@Module
class AppModule(val context: Context) {

    @Provides
    @Singleton
    fun provideContext(): Context = context

    @Provides
    @Singleton
    fun provideJobQueue(context: Context): JobQueue = JobQueue(context)

    @Provides
    @Singleton
    fun providePlayerRepository(): PlayerRepository = RealmPlayerRepository()
}