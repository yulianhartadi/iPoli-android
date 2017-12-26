package mypoli.android.pet

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.pet.PetViewController.ItemComparisonViewModel.Change.*
import mypoli.android.pet.PetViewState.StateType.*
import mypoli.android.pet.usecase.*
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/24/17.
 */
class PetPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val feedPetUseCase: FeedPetUseCase,
    private val renamePetUseCase: RenamePetUseCase,
    private val revivePetUseCase: RevivePetUseCase,
    private val comparePetItemsUseCase: ComparePetItemsUseCase,
    private val buyPetItemUseCase: BuyPetItemUseCase,
    private val equipPetItemUseCase: EquipPetItemUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetViewState>, PetViewState, PetIntent>(
    PetViewState(LOADING, reviveCost = Constants.REVIVE_PET_GEM_PRICE),
    coroutineContext
) {
    override fun reduceState(intent: PetIntent, state: PetViewState) =
        when (intent) {
            is LoadDataIntent -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        sendChannel.send(ChangePlayerIntent(it))
                    }
                }
                state
            }
            is ShowFoodListIntent -> {
                state.copy(
                    type = FOOD_LIST_SHOWN
                )
            }

            is HideFoodListIntent -> {
                state.copy(
                    type = FOOD_LIST_HIDDEN
                )
            }

            is FeedIntent -> {
                val result = feedPetUseCase.execute(Parameters(intent.food))
                when (result) {
                    is Result.TooExpensive -> state.copy(type = FOOD_TOO_EXPENSIVE)
                    is Result.PetFed -> {

                        val pet = result.player.pet
                        state.copy(
                            type = PET_FED,
                            food = intent.food,
                            wasFoodTasty = result.wasFoodTasty,
                            mood = pet.mood,
                            isDead = pet.isDead
                        )
                    }
                }
            }

            is ChangePlayerIntent -> {
                val player = intent.player
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
                val itemVms = state.currentItemsType?.let {
                    val itemType = it
                    val equippedPetItems = listOfNotNull(equipment.hat, equipment.mask, equipment.bodyArmor).toSet()
                    createPetItemViewModels(
                        itemType,
                        PetItem.values().first { it.type == itemType },
                        boughtItems,
                        equippedPetItems
                    )
                } ?: listOf()

                state.copy(
                    type = type,
                    petName = pet.name,
                    stateName = pet.mood.name.toLowerCase().capitalize(),
                    equippedHatItem = toItemViewModel(equipment.hat),
                    equippedMaskItem = toItemViewModel(equipment.mask),
                    equippedBodyArmorItem = toItemViewModel(equipment.bodyArmor),
                    mp = pet.moodPoints,
                    hp = pet.healthPoints,
                    coinsBonus = pet.coinBonus,
                    xpBonus = pet.experienceBonus,
                    unlockChanceBonus = pet.bountyBonus,
                    avatar = pet.avatar,
                    mood = pet.mood,
                    isDead = pet.isDead,
                    playerGems = player.gems,
                    itemViewModels = itemVms,
                    foodViewModels = createFoodViewModels(food),
                    boughtItems = boughtItems
                )
            }

            is RenamePetRequestIntent -> {
                state.copy(
                    type = RENAME_PET
                )
            }

            is RenamePetIntent -> {
                renamePetUseCase.execute(RenamePetUseCase.Params(intent.name))
                state.copy(
                    type = PET_RENAMED,
                    petName = intent.name
                )
            }

            is RevivePetIntent -> {
                val reviveResult = revivePetUseCase.execute(Unit)
                when (reviveResult) {
                    is RevivePetUseCase.Result.TooExpensive -> state.copy(
                        type = REVIVE_TOO_EXPENSIVE
                    )
                    else -> {
                        state.copy(
                            type = PET_REVIVED
                        )
                    }
                }
            }

            is PetIntent.ShowCurrencyConverter -> {
                state.copy(
                    type = SHOW_CURRENCY_CONVERTER
                )
            }

            is PetIntent.ShowItemList ->
                changeItemTypeState(state, PetItemType.BODY_ARMOR, ITEM_LIST_SHOWN)

            is PetIntent.CompareItem -> {
                val vms = state.itemViewModels.map {
                    it.copy(
                        isSelected = it.item == intent.newItem
                    )
                }

                val selected = vms.first { it.isSelected }
                val selectedItem = selected.item

                val newItem = PetViewController.CompareItemViewModel(
                    image = selected.image,
                    item = selectedItem,
                    coinBonus = selectedItem.coinBonus,
                    coinBonusChange = changeOf(selectedItem.coinBonus),
                    xpBonus = selectedItem.experienceBonus,
                    xpBonusChange = changeOf(selectedItem.experienceBonus),
                    bountyBonus = selectedItem.bountyBonus,
                    bountyBonusChange = changeOf(selectedItem.bountyBonus),
                    isBought = state.boughtItems.contains(selectedItem)
                )

                val petItems = AndroidPetAvatar.valueOf(state.avatar!!.name).items
                val petCompareItemImage = petItems[newItem.item]
                val itemType = newItem.item.type

                val newItemImage: (PetItemType, PetViewController.EquipmentItemViewModel?) -> Int? =
                    { type, equippedImage ->
                        if (itemType == type) petCompareItemImage else equippedImage?.image
                    }

                val cmpRes = comparePetItemsUseCase.execute(ComparePetItemsUseCase.Params(state.equippedItem?.item, intent.newItem))

                state.copy(
                    type = COMPARE_ITEMS,
                    itemViewModels = vms,
                    newItem = newItem,
                    itemComparison = PetViewController.ItemComparisonViewModel(
                        coinBonusDiff = cmpRes.coinBonus,
                        coinBonusChange = changeOf(cmpRes.coinBonus),
                        xpBonusDiff = cmpRes.experienceBonus,
                        xpBonusChange = changeOf(cmpRes.experienceBonus),
                        bountyBonusDiff = cmpRes.bountyBonus,
                        bountyBonusChange = changeOf(cmpRes.bountyBonus)
                    ),
                    newHatItemImage = newItemImage(PetItemType.HAT, state.equippedHatItem),
                    newMaskItemImage = newItemImage(PetItemType.MASK, state.equippedMaskItem),
                    newBodyArmorItemImage = newItemImage(PetItemType.BODY_ARMOR, state.equippedBodyArmorItem)
                )
            }

            is PetIntent.ShowHeadItemList ->
                changeItemTypeState(state, PetItemType.HAT, CHANGE_ITEM_CATEGORY)

            is PetIntent.ShowFaceItemList ->
                changeItemTypeState(state, PetItemType.MASK, CHANGE_ITEM_CATEGORY)

            is PetIntent.ShowBodyItemList ->
                changeItemTypeState(state, PetItemType.BODY_ARMOR, CHANGE_ITEM_CATEGORY)

            is PetIntent.HideItemList ->
                state.copy(
                    type = ITEM_LIST_HIDDEN
                )

            is PetIntent.BuyItem -> {
                val result = buyPetItemUseCase.execute(BuyPetItemUseCase.Params(intent.item))
                state.copy(
                    type = when (result) {
                        is BuyPetItemUseCase.Result.TooExpensive -> ITEM_TOO_EXPENSIVE
                        is BuyPetItemUseCase.Result.ItemBought -> ITEM_BOUGHT
                    }
                )
            }

            is PetIntent.EquipItem -> {
                equipPetItemUseCase.execute(EquipPetItemUseCase.Params(intent.item))
                state.copy(
                    type = ITEM_EQUIPPED
                )
            }
        }

    private fun changeItemTypeState(state: PetViewState, itemType: PetItemType, stateType: PetViewState.StateType): PetViewState {

        val equippedPetItems = listOfNotNull(state.equippedHatItem?.item, state.equippedMaskItem?.item, state.equippedBodyArmorItem?.item).toSet()

        val vms = createPetItemViewModels(itemType, PetItem.values().first { it.type == itemType }, state.boughtItems, equippedPetItems)

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
            PetViewController.CompareItemViewModel(
                image = AndroidPetItem.valueOf(item.name).image,
                item = item,
                coinBonus = item.coinBonus,
                coinBonusChange = changeOf(item.coinBonus),
                xpBonus = item.experienceBonus,
                xpBonusChange = changeOf(item.experienceBonus),
                bountyBonus = item.bountyBonus,
                bountyBonusChange = changeOf(item.bountyBonus),
                isBought = true
            )
        }

        val selected = vms.first { it.isSelected }
        val selectedItem = selected.item

        val nItem = PetViewController.CompareItemViewModel(
            image = selected.image,
            item = selectedItem,
            coinBonus = selectedItem.coinBonus,
            coinBonusChange = changeOf(selectedItem.coinBonus),
            xpBonus = selectedItem.experienceBonus,
            xpBonusChange = changeOf(selectedItem.experienceBonus),
            bountyBonus = selectedItem.bountyBonus,
            bountyBonusChange = changeOf(selectedItem.bountyBonus),
            isBought = state.boughtItems.contains(selectedItem)
        )

        val cmpRes = comparePetItemsUseCase.execute(ComparePetItemsUseCase.Params(equippedItem?.item, selectedItem))

        val petItems = AndroidPetAvatar.valueOf(state.avatar!!.name).items
        val petCompareItemImage = petItems[nItem.item]
        val newItemType = nItem.item.type

        val newItemImage: (PetItemType, PetViewController.EquipmentItemViewModel?) -> Int? =
            { type, equippedImage ->
                if (newItemType == type) petCompareItemImage else equippedImage?.image
            }

        return state.copy(
            type = stateType,
            itemViewModels = vms,
            equippedItem = equippedItem,
            newItem = nItem,
            currentItemsType = itemType,
            itemComparison = PetViewController.ItemComparisonViewModel(
                coinBonusDiff = cmpRes.coinBonus,
                coinBonusChange = changeOf(cmpRes.coinBonus),
                xpBonusDiff = cmpRes.experienceBonus,
                xpBonusChange = changeOf(cmpRes.experienceBonus),
                bountyBonusDiff = cmpRes.bountyBonus,
                bountyBonusChange = changeOf(cmpRes.bountyBonus)
            ),
            newHatItemImage = newItemImage(PetItemType.HAT, state.equippedHatItem),
            newMaskItemImage = newItemImage(PetItemType.MASK, state.equippedMaskItem),
            newBodyArmorItemImage = newItemImage(PetItemType.BODY_ARMOR, state.equippedBodyArmorItem)
        )
    }

    private fun changeOf(value: Int) =
        when {
            value > 0 -> POSITIVE
            value < 0 -> NEGATIVE
            else -> NO_CHANGE
        }

    private fun createPetItemViewModels(petItemType: PetItemType, selectedItem: PetItem, boughtItems: Set<PetItem>, equippedPetItems: Set<PetItem>) =
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

    private fun createFoodViewModels(inventoryFood: Map<Food, Int>) =
        Food.values().map {
            PetViewController.PetFoodViewModel(
                it.image, it.price, it, inventoryFood[it] ?: 0
            )
        }

}