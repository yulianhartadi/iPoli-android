package io.ipoli.android.pet.sideeffect

import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.pet.PetAction
import io.ipoli.android.pet.PetItem
import io.ipoli.android.pet.PetItemType
import io.ipoli.android.pet.usecase.ComparePetItemsUseCase
import io.ipoli.android.pet.usecase.Parameters
import io.ipoli.android.pet.usecase.RenamePetUseCase
import space.traversal.kapsule.required

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 4/29/18.
 */
class PetSideEffectHandler : AppSideEffectHandler() {

    private val comparePetItemsUseCase by required { comparePetItemsUseCase }
    private val renamePetUseCase by required { renamePetUseCase }
    private val feedPetUseCase by required { feedPetUseCase }
    private val revivePetUseCase by required { revivePetUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when(action) {
//            PetAction.Load -> {
//                val petState = state.stateFor(PetViewState::class.java)
//                if (petState.comparedItemsType != null) {
//
//                }
//            }

            is DataLoadedAction.PlayerChanged -> {

            }

            is PetAction.RenamePet -> {
                renamePetUseCase.execute(RenamePetUseCase.Params(action.name))
            }

            is PetAction.Feed -> {
                val result = feedPetUseCase.execute(Parameters(action.food))
                dispatch(PetAction.PetFedResult(result, action.food))
            }

            is PetAction.Revive -> {
                val result = revivePetUseCase.execute(Unit)
                dispatch(PetAction.ReviveResult(result))
            }

            is PetAction.ShowItemListRequest -> {
//                val petState = state.stateFor(PetViewState::class.java)
//                val equipped =
//// when (PetItemType.BODY_ARMOR) {
////                    PetItemType.HAT -> {
////                        state.equippedHatItem
////                    }
////                    PetItemType.MASK -> {
////                        state.equippedMaskItem
////                    }
////                    PetItemType.BODY_ARMOR -> {
//                    petState.equippedBodyArmorItem
////                    }
//
//                state.dataState.player!!.pet.equipment.bodyArmor

                val selectedItem = PetItem.values().first { it.type == PetItemType.BODY_ARMOR }

                val cmpRes = comparePetItemsUseCase.execute(
                    ComparePetItemsUseCase.Params(
                        state.dataState.player!!.pet.equipment.bodyArmor,
                        selectedItem
                    )
                )

                dispatch(PetAction.ShowItemList(cmpRes))
            }
        }

    }

    override fun canHandle(action: Action) =
        action is PetAction
            || action is DataLoadedAction.PlayerChanged

}