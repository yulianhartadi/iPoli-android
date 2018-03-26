package io.ipoli.android.home

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.home.HomeViewState.Initial
import io.ipoli.android.home.HomeViewState.PlayerChanged
import io.ipoli.android.pet.PetAvatar
import io.ipoli.android.pet.PetMood
import io.ipoli.android.player.Player
import io.ipoli.android.player.data.Avatar

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
                player?.let {
                    createPlayerChangeState(it)
                } ?: Initial(showSignIn = true)
            }

            is DataLoadedAction.PlayerChanged -> {
                createPlayerChangeState(action.player)
            }
            else -> subState
        }

    private fun createPlayerChangeState(player: Player) =
        PlayerChanged(
            showSignIn = !player.isLoggedIn(),
            avatar = player.avatar,
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

    override fun defaultState() = Initial(showSignIn = true)
}

sealed class HomeViewState : ViewState {

    data class Initial(val showSignIn: Boolean) : HomeViewState()

    data class PlayerChanged(
        val showSignIn: Boolean,
        val titleIndex: Int,
        val level: Int,
        val avatar: Avatar,
        val petAvatar: PetAvatar,
        val petMood: PetMood,
        val gems: Int,
        val lifeCoins: Int,
        val experience: Long,
        val progress: Int,
        val maxProgress: Int
    ) : HomeViewState()
}