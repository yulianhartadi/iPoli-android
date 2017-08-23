package io.ipoli.android.challenge.list.di

import dagger.Component
import io.ipoli.android.challenge.list.ChallengeListPresenter
import io.ipoli.android.challenge.list.ui.ChallengeListController
import io.ipoli.android.common.di.ControllerComponent
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/17.
 */
@ChallengeListScope
@Component(modules = arrayOf(ChallengeListModule::class),
    dependencies = arrayOf(ControllerComponent::class))
interface ChallengeListComponent {
    fun inject(controller: ChallengeListController)

    fun createPresenter(): ChallengeListPresenter
}

@Scope
annotation class ChallengeListScope