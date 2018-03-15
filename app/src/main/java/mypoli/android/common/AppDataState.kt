package mypoli.android.common

import mypoli.android.challenge.entity.Challenge
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Reducer
import mypoli.android.common.redux.State
import mypoli.android.event.Calendar
import mypoli.android.event.Event
import mypoli.android.player.Player
import mypoli.android.quest.Quest
import mypoli.android.quest.RepeatingQuest
import mypoli.android.quest.schedule.agenda.usecase.CreateAgendaItemsUseCase
import mypoli.android.quest.usecase.Schedule
import mypoli.android.repeatingquest.usecase.CreateRepeatingQuestHistoryUseCase
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/24/2018.
 */

sealed class DataLoadedAction : Action {
    data class PlayerChanged(val player: Player) : DataLoadedAction()
    data class TodayQuestsChanged(val quests: List<Quest>) : DataLoadedAction()
    data class RepeatingQuestsChanged(val repeatingQuests: List<RepeatingQuest>) :
        DataLoadedAction()

    data class ChallengesChanged(val challenges: List<Challenge>) :
        DataLoadedAction()

    data class AgendaItemsChanged(
        val start: LocalDate,
        val end: LocalDate,
        val agendaItems: List<CreateAgendaItemsUseCase.AgendaItem>,
        val currentAgendaItemDate: LocalDate?
    ) : DataLoadedAction()

    data class CalendarScheduleChanged(val schedule: Map<LocalDate, Schedule>) :
        DataLoadedAction()

    data class RepeatingQuestHistoryChanged(
        val repeatingQuestId: String,
        val history: CreateRepeatingQuestHistoryUseCase.History
    ) : DataLoadedAction()

    data class EventsChanged(val events: List<Event>) : DataLoadedAction()
    data class CalendarsChanged(val calendars: List<Calendar>) : DataLoadedAction()
}

data class AppDataState(
    val today: LocalDate,
    val player: Player?,
    val todayQuests: List<Quest>,
    val calendarSchedule: Map<LocalDate, Schedule>,
    val repeatingQuests: List<RepeatingQuest>,
    val challenges: List<Challenge>,
    val agendaItems: List<CreateAgendaItemsUseCase.AgendaItem>,
    val events: List<Event>
) : State

object AppDataReducer : Reducer<AppState, AppDataState> {

    override val stateKey: String = AppDataState::class.java.simpleName

    override fun reduce(state: AppState, subState: AppDataState, action: Action) =
        when (action) {

            is DataLoadedAction.PlayerChanged -> {
                subState.copy(
                    player = action.player
                )
            }

            is DataLoadedAction.CalendarScheduleChanged ->
                subState.copy(
                    calendarSchedule = action.schedule
                )

            is DataLoadedAction.TodayQuestsChanged ->
                subState.copy(
                    todayQuests = action.quests
                )

            is DataLoadedAction.RepeatingQuestsChanged ->
                subState.copy(
                    repeatingQuests = action.repeatingQuests
                )

            is DataLoadedAction.AgendaItemsChanged ->
                subState.copy(
                    agendaItems = action.agendaItems
                )

            is DataLoadedAction.ChallengesChanged ->
                subState.copy(
                    challenges = action.challenges
                )

            is DataLoadedAction.EventsChanged ->
                subState.copy(
                    events = action.events
                )

            else -> subState
        }

    override fun defaultState(): AppDataState {
        return AppDataState(
            today = LocalDate.now(),
            player = null,
            todayQuests = listOf(),
            calendarSchedule = mapOf(),
            repeatingQuests = listOf(),
            challenges = listOf(),
            agendaItems = listOf(),
            events = listOf()
        )
    }

}