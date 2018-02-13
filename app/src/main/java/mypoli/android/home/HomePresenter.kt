package mypoli.android.home

import android.content.Context
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/12/18.
 */
class HomePresenter : AndroidStatePresenter<AppState, HomeViewState> {
    override fun present(state: AppState, context: Context): HomeViewState {
        val player = state.appDataState.player
        return HomeViewState(
            showSignIn = if (player != null) !player.isLoggedIn() else true
        )
    }
}