package mypoli.android.challenge

import mypoli.android.challenge.PersonalizeChallengeViewController.ChallengeQuestViewModel
import mypoli.android.challenge.data.PredefinedChallenge
import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */

sealed class PersonalizeChallengeIntent : Intent {
    data class LoadData(val challenge: PredefinedChallenge) : PersonalizeChallengeIntent()
    data class AcceptChallenge(val challenge: PredefinedChallenge) : PersonalizeChallengeIntent()
    data class ToggleSelected(val quest: ChallengeQuestViewModel) : PersonalizeChallengeIntent()
}

data class PersonalizeChallengeViewState(
    val type: StateType,
    val viewModels: List<ChallengeQuestViewModel> = listOf()
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        CHALLENGE_ACCEPTED,
        NO_QUESTS_SELECTED,
        TOGGLE_SELECTED_QUEST
    }
}