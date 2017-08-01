package io.ipoli.android.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.Navigator

/**
 * Created by vini on 8/1/17.
 */
@Module
class ControllerModule(private val navigator: Navigator) {

    @Provides
    @ControllerScope
    fun provideNavigator() = navigator
}