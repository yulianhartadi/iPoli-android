package io.ipoli.android.reward.list

import dagger.Component
import io.ipoli.android.common.BaseComponent
import io.ipoli.android.common.di.AppModule
import io.ipoli.android.common.di.ControllerComponent
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/1/17.
 */
@RewardListScope
@Component(modules = arrayOf(RewardListModule::class),
    dependencies = arrayOf(ControllerComponent::class))
interface RewardListComponent : BaseComponent<RewardListController, RewardListPresenter>

@Scope
annotation class RewardListScope