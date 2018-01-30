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
import mypoli.android.quest.agenda.AgendaAction
import mypoli.android.quest.agenda.AgendaReducer
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

        if (action == LoadDataAction.All) {
            launch {
                playerRepository.listen().consumeEach {
                    dispatcher.dispatch(PlayerChanged(it!!))
                }
            }
        }


        var agendaDate = LocalDate.now()
        var start = agendaDate.minusMonths(3)
        var end = agendaDate.plusMonths(3)

        val agendaItems = state.agendaState.agendaItems

        when (action) {
            is AgendaAction.LoadBefore -> {
                val position = action.visiblePosition
                val agendaItem = agendaItems[position]
                agendaDate = findAgendaItemDate(agendaItem)
                val result = findAgendaDatesUseCase.execute(
                    FindAgendaDatesUseCase.Params.Before(
                        agendaDate,
                        AgendaReducer.ITEMS_BEFORE_COUNT
                    )
                )
                (result as FindAgendaDatesUseCase.Result.Before).date?.let {
                    start = it
                }
                end =
                    findAgendaItemDate(agendaItems[position + AgendaReducer.ITEMS_AFTER_COUNT - 1])
            }
            is AgendaAction.LoadAfter -> {
                val position = action.visiblePosition
                val agendaItem = agendaItems[position]
                agendaDate = findAgendaItemDate(agendaItem)
                val result = findAgendaDatesUseCase.execute(
                    FindAgendaDatesUseCase.Params.After(agendaDate, AgendaReducer.ITEMS_AFTER_COUNT)
                )
                start = findAgendaItemDate(agendaItems[position - AgendaReducer.ITEMS_BEFORE_COUNT])
                (result as FindAgendaDatesUseCase.Result.After).date?.let {
                    end = it
                }
            }
            else -> {
                val result = findAgendaDatesUseCase.execute(
                    FindAgendaDatesUseCase.Params.All(
                        agendaDate,
                        AgendaReducer.ITEMS_BEFORE_COUNT,
                        AgendaReducer.ITEMS_AFTER_COUNT
                    )
                ) as FindAgendaDatesUseCase.Result.All
                start = result.start ?: agendaDate.minusMonths(3)
                end = result.end ?: agendaDate.plusMonths(3)
            }
        }


        launch {
            scheduledQuestsChannel?.cancel()
            scheduledQuestsChannel = questRepository.listenForScheduledBetween(
                start,
                end
            )
            scheduledQuestsChannel!!.consumeEach {
                dispatcher.dispatch(
                    AgendaItemsChanged(
                        start, end, createAgendaItemsUseCase.execute(
                            CreateAgendaItemsUseCase.Params(
                                agendaDate,
                                it,
                                AgendaReducer.ITEMS_BEFORE_COUNT,
                                AgendaReducer.ITEMS_AFTER_COUNT
                            )
                        )
                    )
                )
            }
        }

        launch {
            questRepository.listenForScheduledAt(agendaDate).consumeEach {
                dispatcher.dispatch(TodayQuestsChanged(it))
            }
        }

    }

    private fun findAgendaItemDate(agendaItem: CreateAgendaItemsUseCase.AgendaItem) =
        when (agendaItem) {
            is CreateAgendaItemsUseCase.AgendaItem.QuestItem -> agendaItem.quest.scheduledDate
            is CreateAgendaItemsUseCase.AgendaItem.Date -> agendaItem.date
            is CreateAgendaItemsUseCase.AgendaItem.Week -> agendaItem.start
            is CreateAgendaItemsUseCase.AgendaItem.Month ->
                LocalDate.of(agendaItem.month.year, agendaItem.month.month, 1)
        }

    override fun canHandle(action: Action) =
        action == LoadDataAction.All
            || action is AgendaAction.LoadBefore
            || action is AgendaAction.LoadAfter
}