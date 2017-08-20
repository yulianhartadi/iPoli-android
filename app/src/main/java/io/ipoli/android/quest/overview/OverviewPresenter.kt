package io.ipoli.android.quest.overview

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.quest.overview.ui.OverviewController
import io.ipoli.android.quest.overview.ui.OverviewViewState
import javax.inject.Inject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/20/17.
 */
class OverviewPresenter @Inject constructor() : MviBasePresenter<OverviewController, OverviewViewState>() {
    override fun bindIntents() {

    }

}