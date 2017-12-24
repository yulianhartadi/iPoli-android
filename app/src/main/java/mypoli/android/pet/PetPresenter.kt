package mypoli.android.pet

import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.Constants
import mypoli.android.R
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
                val food = intent.player.inventory.food
                val pet = intent.player.pet

                val type = when {
                    state.petName.isEmpty() -> DATA_LOADED
                    else -> PET_CHANGED
                }

                state.copy(
                    type = type,
                    petName = pet.name,
                    stateName = pet.mood.name.toLowerCase().capitalize(),
                    mp = pet.moodPoints,
                    hp = pet.healthPoints,
                    coinsBonus = pet.coinBonus,
                    xpBonus = pet.experienceBonus,
                    unlockChanceBonus = pet.bountyBonus,
                    avatar = pet.avatar,
                    mood = pet.mood,
                    isDead = pet.isDead,
                    playerGems = intent.player.gems,
                    foodViewModels = createFoodViewModels(food)
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

            is PetIntent.ShowItemList -> {

                val vms = listOf(
                    PetViewController.PetItemViewModel(R.drawable.pet_item_body_sweater_red_deer, 2, PetItem.RED_HAT, selected = true),
                    PetViewController.PetItemViewModel(R.drawable.pet_item_body_sweater_red_snowflakes, 2, PetItem.GLASSES),
                    PetViewController.PetItemViewModel(R.drawable.pet_item_body_sweater_red_white, 2, PetItem.MASK)
                )

                val selected = vms.first { it.selected }
                val selectedItem = selected.item

                val compareItem = PetViewController.CompareItemViewModel(
                    image = selected.image,
                    coinBonus = selectedItem.coinBonus,
                    coinBonusChange = changeOf(selectedItem.coinBonus),
                    xpBonus = selectedItem.experienceBonus,
                    xpBonusChange = changeOf(selectedItem.experienceBonus),
                    bountyBonus = selectedItem.bountyBonus,
                    bountyBonusChange = changeOf(selectedItem.bountyBonus)
                )

                state.copy(
                    type = ITEM_LIST_SHOWN,
                    itemViewModels = vms,
                    newItem = compareItem
                )
            }

            is PetIntent.HideItemList -> {
                state.copy(
                    type = ITEM_LIST_HIDDEN
                )
            }

            is PetIntent.CompareItem -> {
                val cmpRes = comparePetItemsUseCase.execute(ComparePetItemsUseCase.Params(null, intent.newItem))
                val vms = state.itemViewModels.map {
                    it.copy(
                        selected = it.item == intent.newItem
                    )
                }

                val selected = vms.first { it.selected }
                val selectedItem = selected.item

                val compareItem = PetViewController.CompareItemViewModel(
                    image = selected.image,
                    coinBonus = selectedItem.coinBonus,
                    coinBonusChange = changeOf(selectedItem.coinBonus),
                    xpBonus = selectedItem.experienceBonus,
                    xpBonusChange = changeOf(selectedItem.experienceBonus),
                    bountyBonus = selectedItem.bountyBonus,
                    bountyBonusChange = changeOf(selectedItem.bountyBonus)
                )

                state.copy(
                    type = COMPARE_ITEM,
                    itemViewModels = vms,
                    newItem = compareItem,
                    itemComparison = PetViewController.ItemComparisonViewModel(
                        coinBonusDiff = cmpRes.coinBonus,
                        coinBonusChange = changeOf(cmpRes.coinBonus),
                        xpBonusDiff = cmpRes.experienceBonus,
                        xpBonusChange = changeOf(cmpRes.experienceBonus),
                        bountyBonusDiff = cmpRes.bountyBonus,
                        bountyBonusChange = changeOf(cmpRes.bountyBonus)
                    )
                )
            }
        }

    private fun changeOf(value: Int) =
        when {
            value > 0 -> POSITIVE
            value < 0 -> NEGATIVE
            else -> NO_CHANGE
        }

    private fun createFoodViewModels(inventoryFood: Map<Food, Int>) =
        Food.values().map {
            PetViewController.PetFoodViewModel(
                it.image, it.price, it, inventoryFood[it] ?: 0
            )
        }

}