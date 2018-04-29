package io.ipoli.android.pet

import android.support.annotation.DrawableRes
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.pet.PetViewState.StateType.*
import io.ipoli.android.pet.usecase.Result
import io.ipoli.android.player.Player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/24/17.
 */
sealed class PetIntent : Intent {
    object ShowItemList : PetIntent()
    object HideItemList : PetIntent()
    data class CompareItem(val newItem: PetItem) : PetIntent()
    object ShowHeadItemList : PetIntent()
    object ShowFaceItemList : PetIntent()
    object ShowBodyItemList : PetIntent()
    data class BuyItem(val item: PetItem) : PetIntent()
    data class EquipItem(val item: PetItem) : PetIntent()
    data class TakeItemOff(val item: PetItem) : PetIntent()
}

object LoadDataIntent : PetIntent()
object ShowFoodListIntent : PetIntent()
object HideFoodListIntent : PetIntent()
object RenamePetRequestIntent : PetIntent()
object RevivePetIntent : PetIntent()
data class RenamePetIntent(val name: String) : PetIntent()
data class FeedIntent(val food: Food) : PetIntent()
data class ChangePlayerIntent(val player: Player) : PetIntent()

sealed class PetAction : Action {
    object Load : PetAction()
    object ShowRenamePet : PetAction()
    data class RenamePet(val name: String) : PetAction()
    data class Feed(val food: Food) : PetAction()
    data class PetFedResult(val result: Result, val food: Food) : PetAction()
}

object PetReducer : BaseViewStateReducer<PetViewState>() {
    override val stateKey = key<PetViewState>()

    override fun reduce(state: AppState, subState: PetViewState, action: Action) =
        when (action) {
            PetAction.Load -> {
                val p = state.dataState.player
                p?.let {
                    createPlayerChangedState(p, subState)
                } ?: subState
            }

            is DataLoadedAction.PlayerChanged -> {
                createPlayerChangedState(action.player, subState)
            }

            is PetAction.RenamePet -> {
                subState.copy(
                    type = PET_RENAMED,
                    petName = action.name
                )
            }

            is PetAction.ShowRenamePet -> {
                subState.copy(
                    type = RENAME_PET
                )
            }

            is PetAction.PetFedResult -> {
                when (action.result) {
                    is Result.TooExpensive -> subState.copy(type = FOOD_TOO_EXPENSIVE)
                    is Result.PetFed -> {
                        val result = action.result
                        val pet = result.player.pet
                        subState.copy(
                            type = PET_FED,
                            food = action.food,
                            wasFoodTasty = result.wasFoodTasty,
                            mood = pet.mood,
                            isDead = pet.isDead
                        )
                    }
                }
            }

            else -> subState
        }

    private fun createPlayerChangedState(player: Player, state: PetViewState): PetViewState {
        val food = player.inventory.food
        val pet = player.pet

        val type = when {
            state.petName.isEmpty() -> DATA_LOADED
            else -> PET_CHANGED
        }

        val equipment = pet.equipment

        val petItems = AndroidPetAvatar.valueOf(pet.avatar.name).items

        val toItemViewModel: (PetItem?) -> PetViewController.EquipmentItemViewModel? = {
            it?.let {
                PetViewController.EquipmentItemViewModel(petItems[it]!!, it)
            }
        }

        val boughtItems = player.inventory.getPet(pet.avatar).items

        val newState = state.copy(
            petName = pet.name,
            stateName = pet.mood.name.toLowerCase().capitalize(),
            equippedHatItem = toItemViewModel(equipment.hat),
            equippedMaskItem = toItemViewModel(equipment.mask),
            equippedBodyArmorItem = toItemViewModel(equipment.bodyArmor),
            mp = pet.moodPoints,
            hp = pet.healthPoints,
            coinsBonus = pet.coinBonus,
            xpBonus = pet.experienceBonus,
            unlockChanceBonus = pet.itemDropBonus,
            avatar = pet.avatar,
            mood = pet.mood,
            isDead = pet.isDead,
            playerGems = player.gems,
            foodViewModels = createFoodViewModels(food),
            boughtItems = boughtItems
        )

        if (state.comparedItemsType != null) {

            return changeItemTypeState(
                state = newState,
                itemType = state.comparedItemsType,
                stateType = type,
                selectedItem = state.itemViewModels.first { it.isSelected }.item
            )
        } else {
            return newState.copy(
                type = type
            )
        }
    }

    private fun changeItemTypeState(
        state: PetViewState,
        itemType: PetItemType,
        stateType: PetViewState.StateType,
        selectedItem: PetItem = PetItem.values().first { it.type == itemType }
    ): PetViewState {

        val equippedPetItems = listOfNotNull(
            state.equippedHatItem?.item,
            state.equippedMaskItem?.item,
            state.equippedBodyArmorItem?.item
        ).toSet()

        val vms =
            createPetItemViewModels(itemType, selectedItem, state.boughtItems, equippedPetItems)

        val equipped = when (itemType) {
            PetItemType.HAT -> {
                state.equippedHatItem
            }
            PetItemType.MASK -> {
                state.equippedMaskItem
            }
            PetItemType.BODY_ARMOR -> {
                state.equippedBodyArmorItem
            }
        }

        val equippedItem = equipped?.let {
            val item = it.item
            val androidPetItem = AndroidPetItem.valueOf(item.name)
            PetViewController.CompareItemViewModel(
                image = androidPetItem.image,
                name = androidPetItem.itemName,
                item = item,
                coinBonus = item.coinBonus,
                coinBonusChange = changeOf(item.coinBonus),
                xpBonus = item.experienceBonus,
                xpBonusChange = changeOf(item.experienceBonus),
                bountyBonus = item.bountyBonus,
                bountyBonusChange = changeOf(item.bountyBonus),
                isBought = true,
                isEquipped = true
            )
        }

        val selectedVM = vms.first { it.isSelected }
        val androidPetItem = AndroidPetItem.valueOf(selectedVM.item.name)
        val newItem = PetViewController.CompareItemViewModel(
            image = selectedVM.image,
            name = androidPetItem.itemName,
            item = selectedItem,
            coinBonus = selectedItem.coinBonus,
            coinBonusChange = changeOf(selectedItem.coinBonus),
            xpBonus = selectedItem.experienceBonus,
            xpBonusChange = changeOf(selectedItem.experienceBonus),
            bountyBonus = selectedItem.bountyBonus,
            bountyBonusChange = changeOf(selectedItem.bountyBonus),
            isBought = state.boughtItems.contains(selectedItem),
            isEquipped = equippedPetItems.contains(selectedItem)
        )

//        val cmpRes = comparePetItemsUseCase.execute(
//            ComparePetItemsUseCase.Params(
//                equippedItem?.item,
//                selectedItem
//            )
//        )

        val petItems = AndroidPetAvatar.valueOf(state.avatar!!.name).items
        val petCompareItemImage = petItems[newItem.item]
        val newItemType = newItem.item.type

        val newItemImage: (PetItemType, PetViewController.EquipmentItemViewModel?) -> Int? =
            { type, equippedImage ->
                if (newItemType == type) petCompareItemImage else equippedImage?.image
            }

        return state.copy(
            type = stateType,
            itemViewModels = vms,
            equippedItem = equippedItem,
            newItem = newItem,
            comparedItemsType = itemType,
//            itemComparison = PetViewController.ItemComparisonViewModel(
//                coinBonusDiff = cmpRes.coinBonus,
//                coinBonusChange = changeOf(cmpRes.coinBonus),
//                xpBonusDiff = cmpRes.experienceBonus,
//                xpBonusChange = changeOf(cmpRes.experienceBonus),
//                bountyBonusDiff = cmpRes.bountyBonus,
//                bountyBonusChange = changeOf(cmpRes.bountyBonus)
//            ),
            newHatItemImage = newItemImage(PetItemType.HAT, state.equippedHatItem),
            newMaskItemImage = newItemImage(PetItemType.MASK, state.equippedMaskItem),
            newBodyArmorItemImage = newItemImage(
                PetItemType.BODY_ARMOR,
                state.equippedBodyArmorItem
            )
        )
    }

    private fun createFoodViewModels(inventoryFood: Map<Food, Int>) =
        Food.values().map {
            PetViewController.PetFoodViewModel(
                it.image, it.price, it, inventoryFood[it] ?: 0
            )
        }

    private fun changeOf(value: Int) =
        when {
            value > 0 -> PetViewController.ItemComparisonViewModel.Change.POSITIVE
            value < 0 -> PetViewController.ItemComparisonViewModel.Change.NEGATIVE
            else -> PetViewController.ItemComparisonViewModel.Change.NO_CHANGE
        }

    private fun createPetItemViewModels(
        petItemType: PetItemType,
        selectedItem: PetItem,
        boughtItems: Set<PetItem>,
        equippedPetItems: Set<PetItem>
    ) =
        PetItem.values()
            .filter { it.type == petItemType }
            .map {
                PetViewController.PetItemViewModel(
                    image = AndroidPetItem.valueOf(it.name).image,
                    gemPrice = it.gemPrice,
                    item = it,
                    isSelected = it == selectedItem,
                    isBought = boughtItems.contains(it),
                    isEquipped = equippedPetItems.contains(it)
                )
            }

    override fun defaultState() =
        PetViewState(type = LOADING, reviveCost = 0)


}

data class PetViewState(
    val type: StateType,
    val reviveCost: Int,
    val petName: String = "",
    val stateName: String = "",
    val food: Food? = null,
    val wasFoodTasty: Boolean = false,
    val maxHP: Int = Pet.MAX_HP,
    val maxMP: Int = Pet.MAX_MP,
    val hp: Int = 0,
    val mp: Int = 0,
    val coinsBonus: Float = 0f,
    val xpBonus: Float = 0f,
    val unlockChanceBonus: Float = 0f,
    val avatar: PetAvatar? = null,
    val mood: PetMood? = null,
    val isDead: Boolean = false,
    val equippedHatItem: PetViewController.EquipmentItemViewModel? = null,
    val equippedMaskItem: PetViewController.EquipmentItemViewModel? = null,
    val equippedBodyArmorItem: PetViewController.EquipmentItemViewModel? = null,
    @DrawableRes val newHatItemImage: Int? = null,
    @DrawableRes val newMaskItemImage: Int? = null,
    @DrawableRes val newBodyArmorItemImage: Int? = null,
    val foodViewModels: List<PetViewController.PetFoodViewModel> = listOf(),
    val itemViewModels: List<PetViewController.PetItemViewModel> = listOf(),
    val equippedItem: PetViewController.CompareItemViewModel? = null,
    val newItem: PetViewController.CompareItemViewModel? = null,
    val itemComparison: PetViewController.ItemComparisonViewModel? = null,
    val comparedItemsType: PetItemType? = null,
    val boughtItems: Set<PetItem> = setOf(),
    val playerGems: Int = 0
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, FOOD_LIST_SHOWN, FOOD_LIST_HIDDEN, PET_FED,
        FOOD_TOO_EXPENSIVE, PET_CHANGED, RENAME_PET, PET_RENAMED,
        PET_REVIVED, REVIVE_TOO_EXPENSIVE,
        ITEM_LIST_SHOWN, ITEM_LIST_HIDDEN, COMPARE_ITEMS, CHANGE_ITEM_CATEGORY,
        ITEM_TOO_EXPENSIVE, ITEM_BOUGHT, ITEM_EQUIPPED, ITEM_TAKEN_OFF
    }
}