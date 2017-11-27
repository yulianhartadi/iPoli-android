package io.ipoli.android.home

import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/15/17.
 */

sealed class HomeIntent : Intent

object LoadDataIntent : HomeIntent()

data class HomeViewState(
    val type: StateType = StateType.DATA_LOADED
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED
    }
}