package io.ipoli.android.challenge.list

import com.hannesdorfmann.mosby3.mvi.MviBasePresenter
import io.ipoli.android.challenge.list.ui.ChallengeListController
import io.ipoli.android.challenge.list.usecase.ChallengeListViewState
import io.ipoli.android.challenge.list.usecase.DisplayChallengeListUseCase
import javax.inject.Inject

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/23/17.
 */
class ChallengeListPresenter @Inject constructor(private val displayChallengeListUseCase: DisplayChallengeListUseCase) :
    MviBasePresenter<ChallengeListController, ChallengeListViewState>() {

    override fun bindIntents() {
        val loadRepeatingQuestList = intent { it.loadChallengesIntent() }
            .flatMap { displayChallengeListUseCase.execute(Unit) }

        subscribeViewState(loadRepeatingQuestList, ChallengeListController::render)
    }
}