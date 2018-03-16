package mypoli.android.home

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.pet.PetAvatar
import mypoli.android.pet.PetMood
import mypoli.android.player.Player

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/12/18.
 */
sealed class HomeAction : Action {
    object Load : HomeAction()
}

object HomeReducer : BaseViewStateReducer<HomeViewState>() {

    override val stateKey = key<HomeViewState>()

    override fun reduce(state: AppState, subState: HomeViewState, action: Action) =
        when (action) {
            is HomeAction.Load -> {
                val player = state.dataState.player
                if (player != null) {
                    createPlayerChangeState(player)
                } else {
                    HomeViewState.Initial(
                        showSignIn = true
                    )
                }
            }

            is DataLoadedAction.PlayerChanged -> {
                createPlayerChangeState(action.player)
            }
            else -> subState
        }

    private fun createPlayerChangeState(player: Player): HomeViewState.PlayerChanged {
        return HomeViewState.PlayerChanged(
            showSignIn = !player.isLoggedIn(),
            petAvatar = player.pet.avatar,
            petMood = player.pet.mood,
            gems = player.gems,
            lifeCoins = player.coins,
            experience = player.experience,
            titleIndex = player.level / 10,
            level = player.level,
            progress = player.experienceProgressForLevel,
            maxProgress = player.experienceForNextLevel
        )
    }

    override fun defaultState() = HomeViewState.Initial(showSignIn = true)
}

sealed class HomeViewState : ViewState {
    data class Initial(val showSignIn: Boolean) : HomeViewState()

    data class PlayerChanged(
        val showSignIn: Boolean,
        val titleIndex: Int,
        val level: Int,
        val petAvatar: PetAvatar,
        val petMood: PetMood,
        val gems: Int,
        val lifeCoins: Int,
        val experience: Long,
        val progress: Int,
        val maxProgress: Int
    ) : HomeViewState()
}