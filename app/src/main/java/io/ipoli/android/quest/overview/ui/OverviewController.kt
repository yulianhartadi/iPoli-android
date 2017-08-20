package io.ipoli.android.quest.overview.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.ipoli.android.R
import io.ipoli.android.common.BaseController
import io.ipoli.android.daggerComponent
import io.ipoli.android.quest.overview.OverviewPresenter
import io.ipoli.android.quest.overview.di.DaggerOverviewComponent
import io.ipoli.android.quest.overview.di.OverviewComponent

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/17.
 */
class OverviewController : BaseController<OverviewController, OverviewPresenter>() {

    val controllerComponent: OverviewComponent by lazy {
        val component = DaggerOverviewComponent.builder()
            .controllerComponent(daggerComponent)
            .build()
        component.inject(this@OverviewController)
        component
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        controllerComponent
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedViewState: Bundle?): View {
        return inflater.inflate(R.layout.controller_overview, container, false)
    }

    override fun setRestoringViewState(restoringViewState: Boolean) {

    }

    override fun createPresenter(): OverviewPresenter = controllerComponent.createPresenter()

}

class OverviewViewState