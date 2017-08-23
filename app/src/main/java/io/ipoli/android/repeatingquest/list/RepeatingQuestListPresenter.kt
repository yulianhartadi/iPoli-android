package io.ipoli.android.repeatingquest.list

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.repeatingquest.list.ui.RepeatingQuestListController
import io.ipoli.android.repeatingquest.list.usecase.DisplayRepeatingQuestListUseCase
import io.ipoli.android.repeatingquest.list.usecase.RepeatingQuestListViewState
import javax.inject.Inject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/22/17.
 */
class RepeatingQuestListPresenter @Inject constructor(private val displayRepeatingQuestListUseCase: DisplayRepeatingQuestListUseCase)
    : MviBasePresenter<RepeatingQuestListController, RepeatingQuestListViewState>() {

    override fun bindIntents() {
        val loadRepeatingQuestList = intent { it.loadRepeatingQuestsIntent() }
            .flatMap { displayRepeatingQuestListUseCase.execute(Unit) }

        subscribeViewState(loadRepeatingQuestList, RepeatingQuestListController::render)
    }
}