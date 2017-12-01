package io.ipoli.android.pet

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.PetViewState.StateType.*
import io.ipoli.android.pet.usecase.FeedPetUseCase
import io.ipoli.android.pet.usecase.ListenForPetChangesUseCase
import io.ipoli.android.pet.usecase.Parameters
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
class PetPresenter(
    private val listenForPetChangesUseCase: ListenForPetChangesUseCase,
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
                    listenForPetChangesUseCase.execute(Unit).consumeEach {
                        actor.send(ChangePetIntent(it))
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
                feedPetUseCase.execute(Parameters(intent.food))
                state.copy(
                    type = PET_FED,
                    foodImage = intent.food.image
                )
            }

            is ChangePetIntent -> {
                val pet = intent.pet
                val petAvatar = AndroidPetAvatar.valueOf(pet.avatar.name)
                state.copy(
                    type = PET_CHANGED,
                    stateName = pet.mood.name.toLowerCase().capitalize(),
                    mp = pet.moodPoints,
                    hp = pet.healthPoints,
                    coinsBonus = pet.coinBonus,
                    xpBonus = pet.experienceBonus,
                    unlockChanceBonus = pet.unlockChanceBonus,
                    image = petAvatar.image,
                    stateImage = petAvatar.moodImage[pet.mood]!!,
                    awesomeStateImage = petAvatar.moodImage[PetMood.AWESOME]!!
                )
            }
        }

}