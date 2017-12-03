package io.ipoli.android.pet

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.PetViewState.StateType.*
import io.ipoli.android.pet.usecase.FeedPetUseCase
import io.ipoli.android.pet.usecase.Parameters
import io.ipoli.android.pet.usecase.Result
import io.ipoli.android.player.usecase.ListenForPlayerChangesUseCase
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
class PetPresenter(
    private val listenForPlayerChangesUseCase: ListenForPlayerChangesUseCase,
    private val feedPetUseCase: FeedPetUseCase,
    coroutineContext: CoroutineContext
) : BaseMviPresenter<ViewStateRenderer<PetViewState>, PetViewState, PetIntent>(
    PetViewState(LOADING),
    coroutineContext
) {
    override fun reduceState(intent: PetIntent, state: PetViewState) =
        when (intent) {
            is LoadDataIntent -> {
                launch {
                    listenForPlayerChangesUseCase.execute(Unit).consumeEach {
                        actor.send(ChangePlayerIntent(it))
                    }
                }
                state.copy(
                    type = DATA_LOADED
                )
            }
            is ShowFoodList -> {
                state.copy(
                    type = FOOD_LIST_SHOWN
                )
            }

            is HideFoodList -> {
                state.copy(
                    type = FOOD_LIST_HIDDEN
                )
            }

            is Feed -> {
                val result = feedPetUseCase.execute(Parameters(intent.food))
                when (result) {
                    is Result.NotEnoughCoins -> state.copy(type = FOOD_TOO_EXPENSIVE)
                    is Result.PetFed -> {

                        state.copy(
                            type = PET_FED,
                            food = intent.food,
                            wasFoodTasty = result.wasFoodTasty,
                            mood = result.player.pet.mood
                        )
                    }
                }
            }

            is ChangePlayerIntent -> {
                val food = intent.player.inventory.food
                val pet = intent.player.pet
                state.copy(
                    type = PET_CHANGED,
                    petName = pet.name,
                    stateName = pet.mood.name.toLowerCase().capitalize(),
                    mp = pet.moodPoints,
                    hp = pet.healthPoints,
                    coinsBonus = pet.coinBonus,
                    xpBonus = pet.experienceBonus,
                    unlockChanceBonus = pet.unlockChanceBonus,
                    avatar = pet.avatar,
                    mood = pet.mood,
                    foodViewModels = createFoodViewModels(food)
                )
            }

            is RenamePetRequest -> {
                state.copy(
                    type = RENAME_PET
                )
            }

            is RenamePet -> {
                if (intent.name.isNotEmpty()) {
                    state.copy(
                        type = PET_RENAMED,
                        petName = intent.name
                    )
                } else {
                    state
                }
            }
        }

    private fun createFoodViewModels(inventoryFood: Map<Food, Int>) =
        Food.values().map {
            PetViewController.PetFoodViewModel(
                it.image, it.price, it, inventoryFood.getOrDefault(it, 0)
            )
        }

}