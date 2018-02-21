package mypoli.android.home

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.pet.PetAvatar
import mypoli.android.pet.PetMood

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/12/18.
 */
sealed class HomeAction : Action

object HomeReducer : BaseViewStateReducer<HomeViewState>() {

    override val stateKey = key<HomeViewState>()

    override fun reduce(state: AppState, subState: HomeViewState, action: Action) =
        when (action) {
            is DataLoadedAction.PlayerChanged -> {
                val player = action.player
                HomeViewState.PlayerChanged(
                    showSignIn = player.isLoggedIn(),
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
            else -> {
                state.dataState.player.let {
                    HomeViewState.Initial(
                        showSignIn = if (it != null) !it.isLoggedIn() else true
                    )
                }
            }
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