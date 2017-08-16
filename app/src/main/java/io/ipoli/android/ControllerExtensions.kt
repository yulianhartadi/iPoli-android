package io.ipoli.android

import com.bluelinelabs.conductor.Controller
import io.ipoli.android.di.ControllerComponent
import io.ipoli.android.di.ControllerModule
import io.ipoli.android.di.DaggerControllerComponent

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/2/17.
 */

val Controller.daggerComponent: ControllerComponent
    get() = DaggerControllerComponent
            .builder()
            .appComponent(iPoliApp.getComponent(applicationContext!!))
            .controllerModule(ControllerModule(Navigator(router)))
            .build()


val Controller.navigator: Navigator
    get() = daggerComponent.navigator()
