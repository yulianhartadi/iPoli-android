package mypoli.android.store.avatar

import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.DataLoadedAction
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.player.Player
import mypoli.android.player.data.Avatar
import mypoli.android.store.avatar.sideeffect.BuyAvatarCompletedAction
import mypoli.android.store.avatar.usecase.BuyAvatarUseCase

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/24/2018.
 */

sealed class AvatarStoreAction : Action {
    object Load : AvatarStoreAction()
    data class Buy(val avatar: Avatar) : AvatarStoreAction()
    data class Change(val avatar: Avatar) : AvatarStoreAction()
}

object AvatarStoreReducer : BaseViewStateReducer<AvatarStoreViewState>() {

    override val stateKey = key<AvatarStoreViewState>()

    override fun reduce(
        state: AppState,
        subState: AvatarStoreViewState,
        action: Action
    ) =
        when (action) {

            AvatarStoreAction.Load ->
                state.dataState.player?.let {
                    AvatarStoreViewState.Changed(createAvatars(it))
                } ?: AvatarStoreViewState.Loading

            is DataLoadedAction.PlayerChanged ->
                AvatarStoreViewState.Changed(createAvatars(action.player))

            is BuyAvatarCompletedAction ->
                when (action.result) {
                    is BuyAvatarUseCase.Result.Bought ->
                        AvatarStoreViewState.AvatarBought

                    is BuyAvatarUseCase.Result.TooExpensive ->
                        AvatarStoreViewState.AvatarTooExpensive
                }

            else -> subState
        }

    override fun defaultState() = AvatarStoreViewState.Loading

    private fun createAvatars(player: Player) =
        Avatar.values().map {
            when {
                player.avatar == it ->
                    AvatarItem.Current(it)

                player.inventory.hasAvatar(it) ->
                    AvatarItem.Bought(it)

                else -> AvatarItem.ForSale(it)
            }
        }
}

sealed class AvatarItem(open val avatar: Avatar) {
    data class Current(override val avatar: Avatar) : AvatarItem(avatar)
    data class Bought(override val avatar: Avatar) : AvatarItem(avatar)
    data class ForSale(override val avatar: Avatar) : AvatarItem(avatar)
}

sealed class AvatarStoreViewState : ViewState {
    object Loading : AvatarStoreViewState()
    object AvatarBought : AvatarStoreViewState()
    object AvatarTooExpensive : AvatarStoreViewState()
    data class Changed(val avatars: List<AvatarItem>) : AvatarStoreViewState()
}