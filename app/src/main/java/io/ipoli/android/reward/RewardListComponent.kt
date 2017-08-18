package io.ipoli.android.reward

import dagger.Component
import io.ipoli.android.common.di.ControllerComponent
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@curiousily.com> on 8/1/17.
 */
@RewardListScope
@Component(modules = arrayOf(RewardListModule::class),
        dependencies = arrayOf(ControllerComponent::class))
interface RewardListComponent {

    fun inject(controller: RewardListController)

    fun createRewardListPresenter(): RewardListPresenter
}

@Scope
annotation class RewardListScope