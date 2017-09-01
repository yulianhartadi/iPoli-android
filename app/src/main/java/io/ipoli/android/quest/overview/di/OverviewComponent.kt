package io.ipoli.android.quest.overview.di

import dagger.Component
import io.ipoli.android.common.BaseComponent
import io.ipoli.android.common.di.ControllerComponent
import io.ipoli.android.quest.overview.OverviewPresenter
import io.ipoli.android.quest.overview.ui.OverviewController
import javax.inject.Scope

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/20/17.
 */
@OverviewScope
@Component(modules = arrayOf(OverviewModule::class),
    dependencies = arrayOf(ControllerComponent::class))
interface OverviewComponent : BaseComponent<OverviewController, OverviewPresenter>

@Scope
annotation class OverviewScope