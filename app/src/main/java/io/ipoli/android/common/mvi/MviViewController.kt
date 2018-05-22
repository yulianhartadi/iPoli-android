package io.ipoli.android.common.mvi

import io.ipoli.android.common.redux.State
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/8/17.
 */
interface ViewState : State {
    val stateId: String
}

abstract class BaseViewState : ViewState {

    override val stateId = UUID.randomUUID().toString()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ViewState) return false
        return stateId == other.stateId
    }

    override fun hashCode() = stateId.hashCode()
}

interface Intent