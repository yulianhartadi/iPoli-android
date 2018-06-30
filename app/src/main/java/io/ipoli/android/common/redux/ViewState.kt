package io.ipoli.android.common.redux

import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 06/30/2018.
 */
interface ViewState : State {
    val stateId: String

    fun hasSameStateId(other: ViewState) = stateId == other.stateId
}

abstract class BaseViewState : ViewState {
    override val stateId = UUID.randomUUID().toString()
}