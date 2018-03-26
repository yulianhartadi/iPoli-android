package io.ipoli.android.quest.schedule.agenda.sideeffect

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import io.ipoli.android.common.AppSideEffectHandler
import io.ipoli.android.common.AppState
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.LoadDataAction
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.schedule.ScheduleAction
import io.ipoli.android.quest.schedule.ScheduleViewState
import io.ipoli.android.quest.schedule.agenda.AgendaAction
import io.ipoli.android.quest.schedule.agenda.AgendaReducer
import io.ipoli.android.quest.schedule.agenda.AgendaViewState
import io.ipoli.android.quest.schedule.agenda.usecase.CreateAgendaItemsUseCase
import io.ipoli.android.quest.schedule.agenda.usecase.FindAgendaDatesUseCase
import io.ipoli.android.quest.schedule.calendar.CalendarAction
import io.ipoli.android.quest.schedule.calendar.CalendarViewState
import io.ipoli.android.quest.timer.usecase.CompleteTimeRangeUseCase
import io.ipoli.android.quest.usecase.CompleteQuestUseCase
import io.ipoli.android.repeatingquest.usecase.CreatePlaceholderQuestsForRepeatingQuestsUseCase
import org.threeten.bp.LocalDate
import space.traversal.kapsule.required

class AgendaSideEffectHandler : AppSideEffectHandler() {

    private val completeQuestUseCase by required { completeQuestUseCase }
    private val undoCompletedQuestUseCase by required { undoCompletedQuestUseCase }
    private val findAgendaDatesUseCase by required { findAgendaDatesUseCase }
    private val createAgendaItemsUseCase by required { createAgendaItemsUseCase }
    private val questRepository by required { questRepository }
    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }
    private val createPlaceholderQuestsForRepeatingQuestsUseCase by required { createPlaceholderQuestsForRepeatingQuestsUseCase }

    private var agendaItemsChannel: ReceiveChannel<List<Quest>>? = null

    override suspend fun doExecute(action: Action, state: AppState) {

        when (action) {
            is AgendaAction.LoadBefore -> {
                val agendaItems = state.stateFor(AgendaViewState::class.java).agendaItems
                val position = action.itemPosition
                val agendaItem = agendaItems[position]
                val agendaDate = agendaItem.startDate()
                val result = findAgendaDatesUseCase.execute(
                    FindAgendaDatesUseCase.Params.Before(
                        agendaDate,
                        AgendaReducer.ITEMS_BEFORE_COUNT
                    )
                )
                var start = agendaDate.minusMonths(3)
                (result as FindAgendaDatesUseCase.Result.Before).date?.let {
                    start = it
                }
                val end =
                    agendaItems[position + AgendaReducer.ITEMS_AFTER_COUNT - 1].startDate()
                listenForAgendaItems(start, end, agendaDate, false)
            }
            is AgendaAction.LoadAfter -> {
                val agendaItems = state.stateFor(AgendaViewState::class.java).agendaItems
                val position = action.itemPosition
                val agendaItem = agendaItems[position]
                val agendaDate = agendaItem.startDate()
                val result = findAgendaDatesUseCase.execute(
                    FindAgendaDatesUseCase.Params.After(
                        agendaDate,
                        AgendaReducer.ITEMS_AFTER_COUNT
                    )
                )
                val start = agendaItems[position - AgendaReducer.ITEMS_BEFORE_COUNT].startDate()
                var end = agendaDate.plusMonths(3)
                (result as FindAgendaDatesUseCase.Result.After).date?.let {
                    end = it
                }

                listenForAgendaItems(start, end, agendaDate, false)
            }

            is AgendaAction.CompleteQuest -> {
                val adapterPos = action.itemPosition
                val agendaState = state.stateFor(AgendaViewState::class.java)
                val questItem =
                    agendaState.agendaItems[adapterPos] as CreateAgendaItemsUseCase.AgendaItem.QuestItem

                val quest = questItem.quest
                if (quest.isStarted) {
                    completeTimeRangeUseCase.execute(
                        CompleteTimeRangeUseCase.Params(
                            quest.id
                        )
                    )
                } else {
                    completeQuestUseCase.execute(
                        CompleteQuestUseCase.Params.WithQuest(
                            quest
                        )
                    )
                }
            }

            is AgendaAction.UndoCompleteQuest -> {
                val agendaItems = state.stateFor(AgendaViewState::class.java).agendaItems
                val adapterPos = action.itemPosition
                val questItem =
                    agendaItems[adapterPos] as CreateAgendaItemsUseCase.AgendaItem.QuestItem
                undoCompletedQuestUseCase.execute(questItem.quest.id)
            }

            is LoadDataAction.All -> {
                val agendaDate = state.dataState.today
                val pair = findAllAgendaDates(agendaDate)
                val start = pair.first
                val end = pair.second

                listenForAgendaItems(start, end, agendaDate, true)
            }
            is ScheduleAction.ScheduleChangeDate -> {
                val pair = findAllAgendaDates(action.date)
                val start = pair.first
                val end = pair.second
                listenForAgendaItems(start, end, action.date, true)
            }
            is CalendarAction.SwipeChangeDate -> {
                val calendarState = state.stateFor(CalendarViewState::class.java)
                val currentPos = calendarState.adapterPosition
                val newPos = action.adapterPosition
                val scheduleState = state.stateFor(ScheduleViewState::class.java)
                val curDate = scheduleState.currentDate
                val agendaDate = if (newPos < currentPos)
                    curDate.minusDays(1)
                else
                    curDate.plusDays(1)
                val pair = findAllAgendaDates(agendaDate)
                val start = pair.first
                val end = pair.second
                listenForAgendaItems(start, end, agendaDate, true)
            }
        }
    }

    private fun listenForAgendaItems(
        start: LocalDate,
        end: LocalDate,
        agendaDate: LocalDate,
        changeCurrentAgendaItem: Boolean
    ) {

        launch(UI) {
            agendaItemsChannel?.cancel()
            agendaItemsChannel = questRepository.listenForScheduledBetween(
                start,
                end
            )
            agendaItemsChannel!!.consumeEach {
                launch(CommonPool) {

                    val placeholderQuests =
                        createPlaceholderQuestsForRepeatingQuestsUseCase.execute(
                            CreatePlaceholderQuestsForRepeatingQuestsUseCase.Params(
                                startDate = start,
                                endDate = end
                            )
                        )

                    val agendaItems = createAgendaItemsUseCase.execute(
                        CreateAgendaItemsUseCase.Params(
                            agendaDate,
                            it + placeholderQuests,
                            AgendaReducer.ITEMS_BEFORE_COUNT,
                            AgendaReducer.ITEMS_AFTER_COUNT
                        )
                    )


                    dispatch(
                        DataLoadedAction.AgendaItemsChanged(
                            start = start,
                            end = end,
                            agendaItems = agendaItems,
                            currentAgendaItemDate = if (changeCurrentAgendaItem) agendaDate else null
                        )
                    )
                }
            }
        }
    }

    private fun findAllAgendaDates(
        agendaDate: LocalDate
    ): Pair<LocalDate, LocalDate> {
        val result = findAgendaDatesUseCase.execute(
            FindAgendaDatesUseCase.Params.All(
                agendaDate,
                AgendaReducer.ITEMS_BEFORE_COUNT,
                AgendaReducer.ITEMS_AFTER_COUNT
            )
        ) as FindAgendaDatesUseCase.Result.All
        val start = result.start ?: agendaDate.minusMonths(3)
        val end = result.end ?: agendaDate.plusMonths(3)
        return Pair(start, end)
    }

    override fun canHandle(action: Action) =
        action == LoadDataAction.All
            || action is AgendaAction
            || action is ScheduleAction.ScheduleChangeDate
            || action is CalendarAction.SwipeChangeDate
}