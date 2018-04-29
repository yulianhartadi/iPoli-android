package io.ipoli.android.pet

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.mvi.Intent
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.pet.PetViewState.StateType.*
import io.ipoli.android.pet.usecase.ComparePetItemsUseCase
import io.ipoli.android.player.Player
import timber.log.Timber

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
    object ShowItemListRequest : PetAction()
    object HideItemList : PetAction()
    object Revive : PetAction()
    object ShowHeadItemListRequest : PetAction()
    object ShowFaceItemListRequest : PetAction()
    object ShowBodyItemListRequest : PetAction()
    object ItemTooExpensive : PetAction()
    object ItemBought : PetAction()
    object ReviveTooExpensive : PetAction()
    object PetRevived : PetAction()
    object FoodTooExpensive : PetAction()

    data class RenamePet(val name: String) : PetAction()
    data class Feed(val food: Food) : PetAction()
    data class ShowItemList(val cmpRes: ComparePetItemsUseCase.Result) : PetAction()
    data class ShowHeadItemList(val cmpRes: ComparePetItemsUseCase.Result) : PetAction()
    data class ShowFaceItemList(val cmpRes: ComparePetItemsUseCase.Result) : PetAction()
    data class ShowBodyArmorItemList(val cmpRes: ComparePetItemsUseCase.Result) : PetAction()
    data class TakeItemOff(val item: PetItem) : PetAction()
    data class EquipItem(val item: PetItem) : PetAction()
    data class BuyItem(val item: PetItem) : PetAction()
    data class PetFed(val pet: Pet, val food: Food, val wasFoodTasty: Boolean) : PetAction()
    data class CompareItem(val item: PetItem) : PetAction()
    class ItemsCompared(
        val cmpRes: ComparePetItemsUseCase.Result,
        val item: PetItem
    ) : PetAction()
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

            is PetAction.FoodTooExpensive -> {
                subState.copy(
                    type = FOOD_TOO_EXPENSIVE
                )
            }

            is PetAction.PetFed -> {
                val pet = action.pet
                subState.copy(
                    type = PET_FED,
                    food = action.food,
                    wasFoodTasty = action.wasFoodTasty,
                    mood = pet.mood,
                    isDead = pet.isDead
                )
            }

            is PetAction.ShowItemList -> {
                changeItemTypeState(
                    subState,
                    PetItemType.BODY_ARMOR,
                    ITEM_LIST_SHOWN,
                    action.cmpRes
                )
            }

            is PetAction.HideItemList -> {
                subState.copy(
                    type = ITEM_LIST_HIDDEN
                )
            }

            is PetAction.ShowHeadItemList -> {
                changeItemTypeState(
                    subState,
                    PetItemType.HAT,
                    CHANGE_ITEM_CATEGORY,
                    action.cmpRes
                )
            }

            is PetAction.ShowFaceItemList -> {
                changeItemTypeState(
                    subState,
                    PetItemType.MASK,
                    CHANGE_ITEM_CATEGORY,
                    action.cmpRes
                )
            }

            is PetAction.ShowBodyArmorItemList -> {
                changeItemTypeState(
                    subState,
                    PetItemType.BODY_ARMOR,
                    CHANGE_ITEM_CATEGORY,
                    action.cmpRes
                )
            }

            is PetAction.ItemsCompared -> {
//                val vms = subState.itemViewModels.map {
//                    it.copy(
//                        isSelected = it.item == action.item
//                    )
//                }

//                val selected = vms.first { it.isSelected }
                val selectedItem = action.item
                val androidPetItem = AndroidPetItem.valueOf(selectedItem.name)
                val newItem = PetViewController.CompareItemViewModel(
                    image = androidPetItem.image,
                    name = androidPetItem.itemName,
                    item = selectedItem,
                    coinBonus = selectedItem.coinBonus,
                    coinBonusChange = changeOf(selectedItem.coinBonus),
                    xpBonus = selectedItem.experienceBonus,
                    xpBonusChange = changeOf(selectedItem.experienceBonus),
                    bountyBonus = selectedItem.bountyBonus,
                    bountyBonusChange = changeOf(selectedItem.bountyBonus),
                    isBought = subState.boughtItems.contains(selectedItem),
                    isEquipped = selectedItem == subState.equippedItem?.item ?: false
                )

                val petItems = AndroidPetAvatar.valueOf(subState.avatar!!.name).items
                val petCompareItemImage = petItems[newItem.item]
                val itemType = newItem.item.type

                val newItemImage: (PetItemType, PetViewController.EquipmentItemViewModel?) -> Int? =
                    { type, equippedImage ->
                        if (itemType == type) petCompareItemImage else equippedImage?.image
                    }

                val cmpRes = action.cmpRes

                subState.copy(
                    type = COMPARE_ITEMS,
                    newItem = newItem,
                    itemComparison = PetViewController.ItemComparisonViewModel(
                        coinBonusDiff = cmpRes.coinBonus,
                        coinBonusChange = changeOf(cmpRes.coinBonus),
                        xpBonusDiff = cmpRes.experienceBonus,
                        xpBonusChange = changeOf(cmpRes.experienceBonus),
                        bountyBonusDiff = cmpRes.bountyBonus,
                        bountyBonusChange = changeOf(cmpRes.bountyBonus)
                    )
//                    newHatItemImage = newItemImage(PetItemType.HAT, subState.equippedHatItem),
//                    newMaskItemImage = newItemImage(PetItemType.MASK, subState.equippedMaskItem),
//                    newBodyArmorItemImage = newItemImage(
//                        PetItemType.BODY_ARMOR,
//                        subState.equippedBodyArmorItem
//                    )
                )
            }

            is PetAction.PetRevived -> {
                subState.copy(
                    type = PET_REVIVED
                )
            }

            is PetAction.ReviveTooExpensive -> {
                subState.copy(
                    type = REVIVE_TOO_EXPENSIVE
                )
            }

            is PetAction.ItemTooExpensive -> {
                subState.copy(
                    type = ITEM_TOO_EXPENSIVE
                )
            }

            is PetAction.ItemBought -> {
                subState.copy(
                    type = ITEM_BOUGHT
                )
            }

            else -> subState
        }

    private fun createPlayerChangedState(player: Player, state: PetViewState): PetViewState {
        Timber.d("AAAA player change")
        val food = player.inventory.food
        val pet = player.pet

        val type = when {
            state.petName.isEmpty() -> DATA_LOADED
            else -> PET_CHANGED
        }

        val equipment = pet.equipment

        val boughtItems = player.inventory.getPet(pet.avatar).items

        val newState = state.copy(
            petName = pet.name,
            stateName = pet.mood.name.toLowerCase().capitalize(),
            equippedHat = equipment.hat,
            equippedMask = equipment.mask,
            equippedBodyArmor = equipment.bodyArmor,
            mp = pet.moodPoints,
            hp = pet.healthPoints,
            coinsBonus = pet.coinBonus,
            xpBonus = pet.experienceBonus,
            unlockChanceBonus = pet.itemDropBonus,
            avatar = pet.avatar,
            mood = pet.mood,
            isDead = pet.isDead,
            playerGems = player.gems,
            inventoryFood = food,
            boughtItems = boughtItems
        )

//        if (state.selectedItemType != null) {

//            return changeItemTypeState(
//                state = newState,
//                itemType = state.selectedItemType,
//                stateType = type,
//                selectedItem = state.itemViewModels.first { it.isSelected }.item
//            )
//        } else {
            return newState.copy(
                type = type
            )
//        }
    }

    private fun changeItemTypeState(
        state: PetViewState,
        itemType: PetItemType,
        stateType: PetViewState.StateType,
        cmpRes: ComparePetItemsUseCase.Result,
        selectedItem: PetItem = PetItem.values().first { it.type == itemType }
    ): PetViewState {

        val equippedPetItems = listOfNotNull(
            state.equippedHat,
            state.equippedMask,
            state.equippedBodyArmor
        ).toSet()

        val equipped = when (itemType) {
            PetItemType.HAT -> {
                state.equippedHat
            }
            PetItemType.MASK -> {
                state.equippedMask
            }
            PetItemType.BODY_ARMOR -> {
                state.equippedBodyArmor
            }
        }

        val equippedItem = equipped?.let {
            val item = it
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

//        val selectedVM = vms.first { it.isSelected }
        val androidPetItem = AndroidPetItem.valueOf(selectedItem.name)
        val newItem = PetViewController.CompareItemViewModel(
//            image = selectedVM.image,
            image = androidPetItem.image,
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
            equippedItem = equippedItem,
            newItem = newItem,
            selectedItemType = itemType,
            itemComparison = PetViewController.ItemComparisonViewModel(
                coinBonusDiff = cmpRes.coinBonus,
                coinBonusChange = changeOf(cmpRes.coinBonus),
                xpBonusDiff = cmpRes.experienceBonus,
                xpBonusChange = changeOf(cmpRes.experienceBonus),
                bountyBonusDiff = cmpRes.bountyBonus,
                bountyBonusChange = changeOf(cmpRes.bountyBonus)
            )
//            newHatItemImage = newItemImage(PetItemType.HAT, state.equippedHatItem),
//            newMaskItemImage = newItemImage(PetItemType.MASK, state.equippedMaskItem),
//            newBodyArmorItemImage = newItemImage(
//                PetItemType.BODY_ARMOR,
//                state.equippedBodyArmorItem
//            )
        )
    }

    private fun changeOf(value: Int) =
        when {
            value > 0 -> PetViewController.ItemComparisonViewModel.Change.POSITIVE
            value < 0 -> PetViewController.ItemComparisonViewModel.Change.NEGATIVE
            else -> PetViewController.ItemComparisonViewModel.Change.NO_CHANGE
        }

    override fun defaultState() =
        PetViewState(
            type = LOADING,
            reviveCost = 0,
            inventoryFood = emptyMap(),
            selectedItemType = null,
            equippedHat = null,
            equippedMask = null,
            equippedBodyArmor = null
        )


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
    val equippedHat: PetItem?,
    val equippedMask: PetItem?,
    val equippedBodyArmor: PetItem?,
    val inventoryFood: Map<Food, Int>,
    val equippedItem: PetViewController.CompareItemViewModel? = null,
    val newItem: PetViewController.CompareItemViewModel? = null,
    val itemComparison: PetViewController.ItemComparisonViewModel? = null,
    val selectedItemType: PetItemType? = null,
    val boughtItems: Set<PetItem> = setOf(),
    val playerGems: Int = 0
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, PET_FED,
        FOOD_TOO_EXPENSIVE, PET_CHANGED, RENAME_PET, PET_RENAMED,
        PET_REVIVED, REVIVE_TOO_EXPENSIVE,
        ITEM_LIST_SHOWN, ITEM_LIST_HIDDEN, COMPARE_ITEMS, CHANGE_ITEM_CATEGORY,
        ITEM_TOO_EXPENSIVE, ITEM_BOUGHT, ITEM_EQUIPPED, ITEM_TAKEN_OFF
    }
}