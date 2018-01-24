package mypoli.android.quest

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/24/18.
 */
sealed class CompletedQuestIntent : Intent {
    data class LoadData(val questId: String) : CompletedQuestIntent()
}

data class CompletedQuestViewState(
    val type: StateType
) : ViewState {

    enum class StateType {
        LOADING,
        DATA_LOADED
    }
}