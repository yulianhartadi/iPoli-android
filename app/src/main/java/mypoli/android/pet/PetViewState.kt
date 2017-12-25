package mypoli.android.pet

import mypoli.android.common.mvi.Intent
import mypoli.android.common.mvi.ViewState
import mypoli.android.player.Player

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/24/17.
 */
sealed class PetIntent : Intent {
    object ShowCurrencyConverter : PetIntent()
    object ShowItemList : PetIntent()
    object HideItemList : PetIntent()
    data class CompareItem(val newItem: PetItem) : PetIntent()
    object ShowHeadItemList : PetIntent()
    object ShowFaceItemList : PetIntent()
    object ShowBodyItemList : PetIntent()
}

object LoadDataIntent : PetIntent()
object ShowFoodListIntent : PetIntent()
object HideFoodListIntent : PetIntent()
object RenamePetRequestIntent : PetIntent()
object RevivePetIntent : PetIntent()
data class RenamePetIntent(val name: String) : PetIntent()
data class FeedIntent(val food: Food) : PetIntent()
data class ChangePlayerIntent(val player: Player) : PetIntent()

data class PetViewState(
    val type: StateType = StateType.DATA_LOADED,
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
    val hatItem: PetViewController.EquipmentItemViewModel? = null,
    val maskItem: PetViewController.EquipmentItemViewModel? = null,
    val bodyArmorItem: PetViewController.EquipmentItemViewModel? = null,
    val foodViewModels: List<PetViewController.PetFoodViewModel> = listOf(),
    val itemViewModels: List<PetViewController.PetItemViewModel> = listOf(),
    val currentItem: PetViewController.CompareItemViewModel? = null,
    val newItem: PetViewController.CompareItemViewModel? = null,
    val itemComparison: PetViewController.ItemComparisonViewModel? = null,
    val currentItemsType: PetItemType? = null,
    val playerGems: Int = 0
) : ViewState {
    enum class StateType {
        LOADING, DATA_LOADED, FOOD_LIST_SHOWN, FOOD_LIST_HIDDEN, PET_FED,
        FOOD_TOO_EXPENSIVE, PET_CHANGED, RENAME_PET, PET_RENAMED,
        PET_REVIVED, REVIVE_TOO_EXPENSIVE, SHOW_CURRENCY_CONVERTER,
        ITEM_LIST_SHOWN, ITEM_LIST_HIDDEN, COMPARE_ITEMS, CHANGE_ITEM_CATEGORY
    }
}