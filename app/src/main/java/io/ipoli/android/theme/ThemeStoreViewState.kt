package io.ipoli.android.theme

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 12/12/17.
 */
sealed class ThemeStoreIntent : Intent

object LoadDataIntent : ThemeStoreIntent()

data class ThemeStoreViewState(
    val type: StateType = StateType.DATA_LOADED
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED
    }
}