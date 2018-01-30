package mypoli.android.common

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.challenge.category.list.ChallengeListForCategoryAction
import mypoli.android.challenge.usecase.BuyChallengeUseCase
import mypoli.android.common.DataLoadedAction.*
import mypoli.android.common.di.Module
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Dispatcher
import mypoli.android.common.redux.Saga
import mypoli.android.myPoliApp
import mypoli.android.pet.store.PetStoreAction
import mypoli.android.pet.usecase.BuyPetUseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.agenda.usecase.CreateAgendaItemsUseCase
import mypoli.android.quest.agenda.usecase.FindAgendaDatesUseCase
import org.threeten.bp.LocalDate
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/27/2018.
 */
class BuyPredefinedChallengeSaga : Saga<AppState>, Injects<Module> {

    private val buyChallengeUseCase by required { buyChallengeUseCase }

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        inject(myPoliApp.module(myPoliApp.instance))
        val challenge = (action as ChallengeListForCategoryAction.BuyChallenge).challenge
        val result = buyChallengeUseCase.execute(BuyChallengeUseCase.Params(challenge))
        when (result) {
            is BuyChallengeUseCase.Result.ChallengeBought -> {
                dispatcher.dispatch(ChallengeListForCategoryAction.ChallengeBought(challenge))
            }

            BuyChallengeUseCase.Result.TooExpensive -> {
                dispatcher.dispatch(
                    ChallengeListForCategoryAction.ChallengeTooExpensive(
                        challenge
                    )
                )
            }
        }
    }

    override fun canHandle(action: Action) = action is ChallengeListForCategoryAction.BuyChallenge
}

class ChangePetSaga : Saga<AppState>, Injects<Module> {

    private val changePetUseCase by required { changePetUseCase }

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        inject(myPoliApp.module(myPoliApp.instance))
        changePetUseCase.execute((action as PetStoreAction.ChangePet).pet)
    }

    override fun canHandle(action: Action) = action is PetStoreAction.ChangePet

}

class BuyPetSaga : Saga<AppState>, Injects<Module> {
    private val buyPetUseCase by required { buyPetUseCase }

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        inject(myPoliApp.module(myPoliApp.instance))
        val result = buyPetUseCase.execute((action as PetStoreAction.BuyPet).pet)
        when (result) {
            is BuyPetUseCase.Result.PetBought -> {
                dispatcher.dispatch(PetStoreAction.PetBought)
            }
            BuyPetUseCase.Result.TooExpensive -> {
                dispatcher.dispatch(PetStoreAction.PetTooExpensive)
            }
        }
    }

    override fun canHandle(action: Action) = action is PetStoreAction.BuyPet
}

class LoadAllDataSaga : Saga<AppState>, Injects<Module> {

    private var scheduledQuestsChannel: ReceiveChannel<List<Quest>>? = null

    private val playerRepository by required { playerRepository }
    private val questRepository by required { questRepository }

    private val findAgendaDatesUseCase by required { findAgendaDatesUseCase }
    private val createAgendaItemsUseCase by required { createAgendaItemsUseCase }

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        inject(myPoliApp.module(myPoliApp.instance))

        launch {
            playerRepository.listen().consumeEach {
                dispatcher.dispatch(PlayerChanged(it!!))
            }
        }

        val today = LocalDate.now()

        val result = findAgendaDatesUseCase.execute(
            FindAgendaDatesUseCase.Params.All(
                today,
                10,
                25
            )
        ) as FindAgendaDatesUseCase.Result.All

        val start = result.start ?: today.minusMonths(3)
        val end = result.end ?: today.plusMonths(3)

        launch {
            scheduledQuestsChannel = questRepository.listenForScheduledBetween(
                start,
                end
            )
            scheduledQuestsChannel!!.consumeEach {
                dispatcher.dispatch(
                    AgendaItemsChanged(
                        start, end, createAgendaItemsUseCase.execute(
                            CreateAgendaItemsUseCase.Params(
                                today,
                                it,
                                25,
                                10
                            )
                        )
                    )
                )
            }
        }

        launch {
            questRepository.listenForScheduledAt(today).consumeEach {
                dispatcher.dispatch(TodayQuestsChanged(it))
            }
        }

    }

    override fun canHandle(action: Action) = action == LoadDataAction.All

}