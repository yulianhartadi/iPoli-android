package io.ipoli.android.repeatingquest.list.di

import dagger.Component
import io.ipoli.android.common.BaseComponent
import io.ipoli.android.common.di.ControllerComponent
import io.ipoli.android.repeatingquest.list.RepeatingQuestListPresenter
import io.ipoli.android.repeatingquest.list.ui.RepeatingQuestListController
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/22/17.
 */
@RepeatingQuestListScope
@Component(modules = arrayOf(RepeatingQuestListModule::class),
    dependencies = arrayOf(ControllerComponent::class))
interface RepeatingQuestListComponent : BaseComponent<RepeatingQuestListController, RepeatingQuestListPresenter>

@Scope
annotation class RepeatingQuestListScope