package io.ipoli.android.pet

import io.ipoli.android.common.mvi.BaseMviPresenter
import io.ipoli.android.common.mvi.ViewStateRenderer
import io.ipoli.android.pet.PetViewState.StateType.*
import io.ipoli.android.pet.usecase.ListenForPetChangesUseCase
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import kotlin.coroutines.experimental.CoroutineContext

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/24/17.
 */
class PetPresenter(
    val listenForPetChangesUseCase: ListenForPetChangesUseCase,
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
                        actor.send(PetChangedIntent(it))
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
                state.copy(
                    type = PET_FED
                )
            }

            is PetChangedIntent -> {
                val pet = intent.pet
                val petAvatar = AndroidPetAvatar.valueOf(pet.avatar.name)
                state.copy(
                    type = PET_CHANGED,
                    mp = pet.moodPoints,
                    hp = pet.healthPoints,
                    coinsBonus = pet.coinBonus,
                    xpBonus = pet.experienceBonus,
                    unlockChanceBonus = pet.unlockChanceBonus,
                    petImage = petAvatar.image,
                    petStateImage = petAvatar.moodImage[pet.mood]!!,
                    petAwesomeStateImage = petAvatar.moodImage[PetMood.AWESOME]!!
                )
            }
        }

}