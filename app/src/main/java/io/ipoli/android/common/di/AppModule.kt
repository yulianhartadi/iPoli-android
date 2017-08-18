package io.ipoli.android.common.di

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/1/17.
 */
@Module
class AppModule(val context: Context) {

    @Provides
    @Singleton
    fun provideContext(): Context = context
}