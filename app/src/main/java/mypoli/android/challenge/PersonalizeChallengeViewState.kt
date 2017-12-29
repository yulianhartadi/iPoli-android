package mypoli.android.challenge

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 12/29/17.
 */

sealed class PersonalizeChallengeIntent : Intent {

}

data class PersonalizeChallengeViewState(val type: StateType) : ViewState {
    enum class StateType { DATA_LOADED }
}