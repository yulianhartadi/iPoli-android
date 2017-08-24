package io.ipoli.android.common

import com.bluelinelabs.conductor.Controller
import io.ipoli.android.common.di.ControllerComponent
import io.ipoli.android.common.di.ControllerModule
import io.ipoli.android.common.di.DaggerControllerComponent
import io.ipoli.android.common.navigation.Navigator
import io.ipoli.android.iPoliApp

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/2/17.
 */
val Controller.daggerComponent: ControllerComponent
    get() = DaggerControllerComponent
        .builder()
        .appComponent(iPoliApp.getComponent(applicationContext!!))
        .controllerModule(ControllerModule(Navigator(router)))
        .build()

val Controller.navigator: Navigator
    get() = daggerComponent.navigator()
