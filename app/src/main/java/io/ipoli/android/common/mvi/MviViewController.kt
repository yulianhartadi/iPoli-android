package io.ipoli.android.common.mvi

import io.ipoli.android.common.redux.State
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/8/17.
 */
interface ViewState : State {
    val stateId: String

    fun hasSameStateId(other: ViewState) = stateId == other.stateId
}

abstract class BaseViewState : ViewState {
    override val stateId = UUID.randomUUID().toString()
}

interface Intent