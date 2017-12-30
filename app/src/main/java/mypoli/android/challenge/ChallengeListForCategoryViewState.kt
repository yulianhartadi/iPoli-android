package mypoli.android.challenge

import mypoli.android.challenge.data.Challenge
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */
sealed class ChallengeListForCategoryIntent : Intent {
    data class LoadData(val challengeCategory: Challenge.Category) : ChallengeListForCategoryIntent()
}

data class ChallengeListForCategoryViewState(val type: StateType) : ViewState {
    enum class StateType { DATA_LOADED }
}