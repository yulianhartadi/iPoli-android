package io.ipoli.android.player.di

import dagger.Component
import io.ipoli.android.common.BaseComponent
import io.ipoli.android.common.di.ControllerComponent
import io.ipoli.android.player.SignInPresenter
import io.ipoli.android.player.ui.SignInController
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/8/17.
 */
@SignInScope
@Component(modules = arrayOf(SignInModule::class),
    dependencies = arrayOf(ControllerComponent::class))
interface SignInComponent : BaseComponent<SignInController, SignInPresenter>

@Scope
annotation class SignInScope