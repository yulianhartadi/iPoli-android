package io.ipoli.android.common

import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.CompositeState
import io.ipoli.android.common.redux.State
import io.ipoli.android.common.redux.ViewStateReducer

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 01/23/2018.
 */

sealed class LoadDataAction : Action {
    object All : LoadDataAction()

    data class ChangePlayer(val oldPlayerId: String) : LoadDataAction()
}

sealed class UIAction : Action {
    data class Attach<S : CompositeState<S>>(val reducer: ViewStateReducer<S, *>) : UIAction()

    data class Detach<S : CompositeState<S>>(val reducer: ViewStateReducer<S, *>) : UIAction()
}

data class NamespaceAction(val source: Action, val namespace: String) : Action

abstract class BaseViewStateReducer<VS : ViewState> : ViewStateReducer<AppState, VS> {
    inline fun <reified VS> key(): String = VS::class.java.simpleName
}

abstract class NamespaceViewStateReducer<VS : ViewState>(private val namespace: String) :
    BaseViewStateReducer<VS>() {

    override fun reduce(state: AppState, subState: VS, action: Action): VS {
        if (action is NamespaceAction) {
            if (action.namespace != namespace) {
                return subState
            }
            return doReduce(state, subState, action.source)
        }
        return doReduce(state, subState, action)
    }

    abstract fun doReduce(state: AppState, subState: VS, action: Action): VS
}

class AppState(
    data: Map<String, State>
) : CompositeState<AppState>(data) {

    val dataState: AppDataState = stateFor(AppDataState::class.java)

    override fun createWithData(stateData: Map<String, State>) = AppState(stateData)
}