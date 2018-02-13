package mypoli.android.home

import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/12/18.
 */
sealed class HomeAction : Action

class HomeViewState(
    val showSignIn: Boolean
) : ViewState