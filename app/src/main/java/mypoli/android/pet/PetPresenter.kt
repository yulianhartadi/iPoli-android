package mypoli.android.pet

import mypoli.android.Constants
import mypoli.android.common.mvi.BaseMviPresenter
import mypoli.android.common.mvi.ViewStateRenderer
import mypoli.android.pet.PetViewState.StateType.*
import mypoli.android.pet.usecase.*
import mypoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
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
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetViewState>, PetViewState, PetIntent>(
    PetViewState(LOADING, reviveCost = Constants.REVIVE_PET_COST),
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
                    playerCoins = intent.player.coins,
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
        }

    private fun createFoodViewModels(inventoryFood: Map<Food, Int>) =
        Food.values().map {
            PetViewController.PetFoodViewModel(
                it.image, it.price, it, inventoryFood[it] ?: 0
            )
        }

}