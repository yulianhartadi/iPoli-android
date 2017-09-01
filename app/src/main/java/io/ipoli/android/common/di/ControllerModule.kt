package io.ipoli.android.common.di

import dagger.Module
import dagger.Provides
import io.ipoli.android.common.navigation.Navigator

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/1/17.
 */
@Module
class ControllerModule(private val navigator: Navigator) {

    @Provides
    @ControllerScope
    fun provideNavigator() = navigator
}