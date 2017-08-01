package io.ipoli.android.di

import dagger.Component
import io.ipoli.android.reward.RewardListPresenter
import io.ipoli.android.reward.RewardListController
import javax.inject.Scope

/**
 * Created by vini on 8/1/17.
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

