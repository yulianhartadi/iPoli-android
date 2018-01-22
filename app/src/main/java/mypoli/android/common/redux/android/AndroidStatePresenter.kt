package mypoli.android.common.redux.android

import android.content.Context
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.State

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 01/22/2018.
 */
interface AndroidStatePresenter<in S : State, out VS : ViewState> {

    fun present(state: S, context: Context): VS
}