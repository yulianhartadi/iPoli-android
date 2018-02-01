package mypoli.android.common.redux.android

import android.content.Context
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.State

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/22/2018.
 */
interface AndroidStatePresenter<in S : State, VS : ViewState> {

    fun present(state: S, context: Context): VS

    fun presentInitial(state: VS): VS = state
}