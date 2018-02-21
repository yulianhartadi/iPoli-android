package mypoli.android.repeatingquest.show

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.Color

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/21/2018.
 */

sealed class RepeatingQuestAction : Action {
    data class Load(val repeatingQuestId: String) : RepeatingQuestAction()
}

sealed class RepeatingQuestViewState : ViewState {

    object Loading : RepeatingQuestViewState()

    data class Changed(val name: String, val color: Color) : RepeatingQuestViewState()
}

object RepeatingQuestReducer : BaseViewStateReducer<RepeatingQuestViewState>() {

    override val stateKey = key<RepeatingQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingQuestViewState,
        action: Action
    ) = when (action) {
        is RepeatingQuestAction.Load -> {
            RepeatingQuestViewState.Changed("Hello World", Color.BLUE)
        }
        else -> subState
    }

    override fun defaultState() = RepeatingQuestViewState.Loading
}