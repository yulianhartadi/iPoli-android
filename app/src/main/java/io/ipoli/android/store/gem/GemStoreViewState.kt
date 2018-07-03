package io.ipoli.android.store.gem

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.redux.BaseViewState
import io.ipoli.android.pet.PetAvatar

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 27.12.17.
 */

sealed class GemStoreAction : Action {
    data class Load(val gemPacks: List<GemPack>) : GemStoreAction()

    data class GemPackPurchased(val dogUnlocked: Boolean) : GemStoreAction() {
        override fun toMap() = mapOf("dogUnlocked" to dogUnlocked)
    }

    data class GemPackBought(val gemPackType: GemPackType) : GemStoreAction() {
        override fun toMap() = mapOf("gemPack" to gemPackType.name)
    }
}

object GemStoreReducer : BaseViewStateReducer<GemStoreViewState>() {
    override fun reduce(
        state: AppState,
        subState: GemStoreViewState,
        action: Action
    ) =
        when (action) {

            is GemStoreAction.Load ->
                subState.copy(
                    type = GemStoreViewState.StateType.GEM_PACKS_LOADED,
                    isGiftPurchased = state.dataState.player!!.hasPet(PetAvatar.DOG),
                    gemPacks = action.gemPacks
                )

            is DataLoadedAction.PlayerChanged ->
                subState.copy(
                    type = GemStoreViewState.StateType.PLAYER_CHANGED,
                    isGiftPurchased = action.player.hasPet(PetAvatar.DOG)
                )

            is DataLoadedAction.GemPacksLoaded ->
                subState.copy(
                    type = GemStoreViewState.StateType.GEM_PACKS_LOADED,
                    gemPacks = action.gemPacks
                )

            is GemStoreAction.GemPackPurchased -> {
                val type =
                    if (action.dogUnlocked)
                        GemStoreViewState.StateType.DOG_UNLOCKED
                    else
                        GemStoreViewState.StateType.GEM_PACK_PURCHASED
                subState.copy(
                    type = type
                )
            }

            else -> subState
        }

    override fun defaultState() = GemStoreViewState(GemStoreViewState.StateType.LOADING)

    override val stateKey = key<GemStoreViewState>()

}

enum class GemPackType {
    BASIC, SMART, PLATINUM
}

data class GemPack(
    val title: String,
    val shortTitle: String,
    val price: String,
    val gems: Int,
    val type: GemPackType
)

data class GemStoreViewState(
    val type: StateType = StateType.DATA_LOADED,
    val playerGems: Int = 0,
    val isGiftPurchased: Boolean = false,
    val gemPacks: List<GemPack> = listOf()
) : BaseViewState() {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        PLAYER_CHANGED,
        GEM_PACKS_LOADED,
        DOG_UNLOCKED,
        GEM_PACK_PURCHASED
    }
}