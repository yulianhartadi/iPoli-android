package io.ipoli.android.player

import dagger.Component
import io.ipoli.android.common.di.ControllerComponent
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/8/17.
 */
@SignInScope
@Component(modules = arrayOf(SignInModule::class),
        dependencies = arrayOf(ControllerComponent::class))
interface SignInComponent {

    fun inject(controller: SignInController)

    fun createSignInPresenter(): SignInPresenter
}

@Scope
annotation class SignInScope