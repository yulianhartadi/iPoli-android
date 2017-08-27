package io.ipoli.android.reward.edit.di

import dagger.Component
import io.ipoli.android.common.BaseComponent
import io.ipoli.android.common.di.ControllerComponent
import io.ipoli.android.reward.edit.EditRewardController
import io.ipoli.android.reward.edit.EditRewardPresenter
import io.ipoli.android.reward.list.RewardListModule
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/17.
 */
@EditRewardScope
@Component(modules = arrayOf(EditRewardModule::class),
    dependencies = arrayOf(ControllerComponent::class))
interface EditRewardComponent : BaseComponent<EditRewardController, EditRewardPresenter>

@Scope
annotation class EditRewardScope