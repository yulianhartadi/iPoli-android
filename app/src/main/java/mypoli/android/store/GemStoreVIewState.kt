package mypoli.android.store

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 27.12.17.
 */
sealed class GemStoreIntent : Intent {
    object LoadData : GemStoreIntent()
}

data class GemStoreViewState(
    val type: StateType = StateType.DATA_LOADED
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED
    }
}