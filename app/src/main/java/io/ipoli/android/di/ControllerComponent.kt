package io.ipoli.android.di

import dagger.Component
import io.ipoli.android.Navigator

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/1/17.
 */
@Component(modules = arrayOf(ControllerModule::class),
        dependencies = arrayOf(AppComponent::class))
@ControllerScope
interface ControllerComponent {

    fun navigator(): Navigator
}