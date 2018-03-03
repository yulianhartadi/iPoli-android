package mypoli.android.common

import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.launch
import mypoli.android.challenge.category.list.ChallengeListForCategoryAction
import mypoli.android.challenge.usecase.BuyChallengeUseCase
import mypoli.android.common.DataLoadedAction.PlayerChanged
import mypoli.android.common.datetime.Time
import mypoli.android.common.datetime.isBetween
import mypoli.android.common.di.Module
import mypoli.android.common.redux.Action
import mypoli.android.common.redux.Dispatcher
import mypoli.android.common.redux.SideEffect
import mypoli.android.common.view.AppWidgetUtil
import mypoli.android.myPoliApp
import mypoli.android.pet.store.PetStoreAction
import mypoli.android.pet.usecase.BuyPetUseCase
import mypoli.android.player.Player
import mypoli.android.quest.Category
import mypoli.android.quest.Color
import mypoli.android.quest.Quest
import mypoli.android.quest.Reminder
import mypoli.android.quest.schedule.ScheduleAction
import mypoli.android.quest.schedule.ScheduleViewState
import mypoli.android.quest.schedule.agenda.AgendaAction
import mypoli.android.quest.schedule.agenda.AgendaReducer
import mypoli.android.quest.schedule.agenda.AgendaViewState
import mypoli.android.quest.schedule.agenda.usecase.CreateAgendaItemsUseCase
import mypoli.android.quest.schedule.agenda.usecase.FindAgendaDatesUseCase
import mypoli.android.quest.schedule.calendar.CalendarAction
import mypoli.android.quest.schedule.calendar.CalendarViewState
import mypoli.android.quest.schedule.calendar.dayview.view.DayViewAction
import mypoli.android.quest.schedule.calendar.dayview.view.DayViewState
import mypoli.android.quest.usecase.CompleteQuestUseCase
import mypoli.android.quest.usecase.CompleteQuestUseCase.Params.WithQuest
import mypoli.android.quest.usecase.LoadScheduleForDateUseCase
import mypoli.android.quest.usecase.Result
import mypoli.android.quest.usecase.SaveQuestUseCase
import mypoli.android.reminder.view.picker.ReminderViewModel
import mypoli.android.repeatingquest.edit.EditRepeatingQuestAction
import mypoli.android.repeatingquest.edit.EditRepeatingQuestViewState
import mypoli.android.repeatingquest.entity.RepeatingQuest
import mypoli.android.repeatingquest.show.RepeatingQuestAction
import mypoli.android.repeatingquest.usecase.FindNextDateForRepeatingQuestUseCase
import mypoli.android.repeatingquest.usecase.FindPeriodProgressForRepeatingQuestUseCase
import mypoli.android.repeatingquest.usecase.RemoveRepeatingQuestUseCase
import mypoli.android.repeatingquest.usecase.SaveRepeatingQuestUseCase
import mypoli.android.timer.usecase.CompleteTimeRangeUseCase
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import space.traversal.kapsule.Injects
import space.traversal.kapsule.inject
import space.traversal.kapsule.required

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/27/2018.
 */

abstract class AppSideEffect : SideEffect<AppState>,
    Injects<Module> {

    private var dispatcher: Dispatcher? = null

    override suspend fun execute(action: Action, state: AppState, dispatcher: Dispatcher) {
        inject(myPoliApp.module(myPoliApp.instance))
        this.dispatcher = dispatcher
        doExecute(action, state)
    }

    abstract suspend fun doExecute(
        action: Action,
        state: AppState
    )

    fun dispatch(action: Action) {
        dispatcher!!.dispatch(action)
    }
}

class DayViewSideEffect : AppSideEffect() {
    private val saveQuestUseCase by required { saveQuestUseCase }
    private val questRepository by required { questRepository }
    private val removeQuestUseCase by required { removeQuestUseCase }
    private val loadScheduleForDateUseCase by required { loadScheduleForDateUseCase }
    private val undoRemoveQuestUseCase by required { undoRemoveQuestUseCase }

    private var scheduledQuestsChannel: ReceiveChannel<List<Quest>>? = null

    private var startDate: LocalDate? = null
    private var endDate: LocalDate? = null

    override suspend fun doExecute(action: Action, state: AppState) {
        val a = (action as? NamespaceAction)?.source ?: action
        when (a) {

            is LoadDataAction.All ->
                startListenForCalendarQuests(state.dataState.today)

            DayViewAction.AddQuest ->
                saveQuest(state, action)

            DayViewAction.EditQuest ->
                saveQuest(state, action)

            DayViewAction.EditUnscheduledQuest ->
                saveQuest(state, action)

            is DayViewAction.RemoveQuest ->
                removeQuestUseCase.execute(a.questId)

            is DayViewAction.UndoRemoveQuest ->
                undoRemoveQuestUseCase.execute(a.questId)

            is DayViewAction.Load -> {

                if (a.currentDate.isBetween(startDate, endDate)) {
                    return
                }
                startListenForCalendarQuests(a.currentDate)
            }
        }
    }

    private fun startListenForCalendarQuests(currentDate: LocalDate) {
        startDate = currentDate.minusDays(2)
        endDate = currentDate.plusDays(2)
        scheduledQuestsChannel?.cancel()
        launch(UI) {
            scheduledQuestsChannel =
                questRepository.listenForScheduledBetween(startDate!!, endDate!!)
            scheduledQuestsChannel!!.consumeEach {
                val schedule =
                    loadScheduleForDateUseCase.execute(
                        LoadScheduleForDateUseCase.Params(
                            startDate = startDate!!,
                            endDate = endDate!!,
                            quests = it
                        )
                    )
                dispatch(DataLoadedAction.CalendarScheduledChanged(schedule))
            }
        }
    }

    private fun saveQuest(
        state: AppState,
        action: Action
    ) {
        val dayViewState: DayViewState = state.stateFor(
            "${(action as NamespaceAction).namespace}/${DayViewState::class.java.simpleName}"
        )

        val scheduledDate = dayViewState.scheduledDate ?: dayViewState.currentDate
        val reminder = if (dayViewState.startTime != null && dayViewState.reminder != null) {
            createQuestReminder(
                dayViewState.reminder,
                scheduledDate,
                dayViewState.startTime.toMinuteOfDay()
            )
        } else if (dayViewState.editId.isEmpty()) {
            createDefaultReminder(scheduledDate, dayViewState.startTime!!.toMinuteOfDay())
        } else {
            null
        }

        val questParams = SaveQuestUseCase.Parameters(
            id = dayViewState.editId,
            name = dayViewState.name,
            color = dayViewState.color!!,
            icon = dayViewState.icon,
            category = Category("WELLNESS", Color.GREEN),
            scheduledDate = scheduledDate,
            startTime = dayViewState.startTime,
            duration = dayViewState.duration!!,
            reminder = reminder
        )
        val result = saveQuestUseCase.execute(questParams)

        when (result) {
            is Result.Invalid -> {
                dispatch(DayViewAction.SaveInvalidQuest(result))
            }
            else -> dispatch(DayViewAction.QuestSaved)
        }
    }

    private fun createDefaultReminder(scheduledDate: LocalDate, startMinute: Int) =
        Reminder("", Time.of(startMinute), scheduledDate)

    private fun createQuestReminder(
        reminder: ReminderViewModel?,
        scheduledDate: LocalDate,
        eventStartMinute: Int
    ) =
        reminder?.let {
            val time = Time.of(eventStartMinute)
            val questDateTime =
                LocalDateTime.of(scheduledDate, LocalTime.of(time.hours, time.getMinutes()))
            val reminderDateTime = questDateTime.minusMinutes(it.minutesFromStart)
            val toLocalTime = reminderDateTime.toLocalTime()
            Reminder(
                it.message,
                Time.at(toLocalTime.hour, toLocalTime.minute),
                reminderDateTime.toLocalDate()
            )
        }

    override fun canHandle(action: Action): Boolean {
        val a = (action as? NamespaceAction)?.source ?: action
        return a is DayViewAction || a is LoadDataAction.All
    }
}

class CompleteQuestSideEffect : AppSideEffect() {

    private val completeTimeRangeUseCase by required { completeTimeRangeUseCase }
    private val completeQuestUseCase by required { completeQuestUseCase }
    private val undoCompletedQuestUseCase by required { undoCompletedQuestUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is DayViewAction.CompleteQuest -> {
                val questId = action.questId
                if (action.isStarted) {
                    completeTimeRangeUseCase.execute(CompleteTimeRangeUseCase.Params(questId))
                } else {
                    completeQuestUseCase.execute(CompleteQuestUseCase.Params.WithQuestId(questId))
                }
            }

            is DayViewAction.UndoCompleteQuest -> {
                undoCompletedQuestUseCase.execute(action.questId)
            }
        }

    }

    override fun canHandle(action: Action): Boolean {
        val a = (action as? NamespaceAction)?.source ?: action
        return a is DayViewAction.CompleteQuest
            || a is DayViewAction.UndoCompleteQuest
    }
}

class RepeatingQuestSideEffect : AppSideEffect() {
    private val saveRepeatingQuestUseCase by required { saveRepeatingQuestUseCase }
    private val removeRepeatingQuestUseCase by required { removeRepeatingQuestUseCase }
    private val repeatingQuestHistoryUseCase by required { repeatingQuestHistoryUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        when (action) {
            is RepeatingQuestAction.Load -> {

            }

            EditRepeatingQuestAction.Save -> {
                val rqState = state.stateFor(EditRepeatingQuestViewState::class.java)

                val rqParams = SaveRepeatingQuestUseCase.Params(
                    id = rqState.id,
                    name = rqState.name,
                    color = rqState.color,
                    icon = rqState.icon,
                    category = Category("WELLNESS", Color.GREEN),
                    startTime = rqState.startTime,
                    duration = rqState.duration,
                    reminder = rqState.reminder,
                    repeatingPattern = rqState.repeatingPattern
                )
                val result = saveRepeatingQuestUseCase.execute(rqParams)
                when (result) {
                    is SaveRepeatingQuestUseCase.Result.Invalid ->
                        dispatch(EditRepeatingQuestAction.SaveInvalidQuest(result))
                    is SaveRepeatingQuestUseCase.Result.Added ->
                        dispatch(EditRepeatingQuestAction.QuestSaved)
                }
            }

            is RepeatingQuestAction.Remove -> {
                removeRepeatingQuestUseCase.execute(RemoveRepeatingQuestUseCase.Params(action.repeatingQuestId))
            }
        }
    }

    override fun canHandle(action: Action) =
        action == EditRepeatingQuestAction.Save
            || action is RepeatingQuestAction.Remove
            || action is RepeatingQuestAction.Load
}

class BuyPredefinedChallengeSideEffect : AppSideEffect() {

    override suspend fun doExecute(action: Action, state: AppState) {
        val challenge = (action as ChallengeListForCategoryAction.BuyChallenge).challenge
        val result = buyChallengeUseCase.execute(BuyChallengeUseCase.Params(challenge))
        when (result) {
            is BuyChallengeUseCase.Result.ChallengeBought -> {
                dispatch(ChallengeListForCategoryAction.ChallengeBought(challenge))
            }

            BuyChallengeUseCase.Result.TooExpensive -> {
                dispatch(
                    ChallengeListForCategoryAction.ChallengeTooExpensive(
                        challenge
                    )
                )
            }
        }
    }

    private val buyChallengeUseCase by required { buyChallengeUseCase }

    override fun canHandle(action: Action) = action is ChallengeListForCategoryAction.BuyChallenge
}

class ChangePetSideEffect : AppSideEffect() {

    private val changePetUseCase by required { changePetUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        changePetUseCase.execute((action as PetStoreAction.ChangePet).pet)
    }

    override fun canHandle(action: Action) = action is PetStoreAction.ChangePet
}

class BuyPetSideEffect : AppSideEffect() {

    private val buyPetUseCase by required { buyPetUseCase }

    override suspend fun doExecute(action: Action, state: AppState) {
        val result = buyPetUseCase.execute((action as PetStoreAction.BuyPet).pet)
        when (result) {
            is BuyPetUseCase.Result.PetBought -> {
                dispatch(PetStoreAction.PetBought)
            }
            BuyPetUseCase.Result.TooExpensive -> {
                dispatch(PetStoreAction.PetTooExpensive)
            }
        }
    }

    override fun canHandle(action: Action) = action is PetStoreAction.BuyPet
}

class AgendaSideEffect : AppSideEffect() {

    private val completeQuestUseCase by required { completeQuestUseCase }
    private val undoCompletedQuestUseCase by required { undoCompletedQuestUseCase }
    private val findAgendaDatesUseCase by required { findAgendaDatesUseCase }
    private val createAgendaItemsUseCase by required { createAgendaItemsUseCase }
    private val questRepository by required { questRepository }

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
                    FindAgendaDatesUseCase.Params.After(agendaDate, AgendaReducer.ITEMS_AFTER_COUNT)
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
                completeQuestUseCase.execute(WithQuest(questItem.quest))
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

        var isFirstData = true
        launch(UI) {
            agendaItemsChannel?.cancel()
            agendaItemsChannel = questRepository.listenForScheduledBetween(
                start,
                end
            )
            agendaItemsChannel!!.consumeEach {
                val agendaItems = createAgendaItemsUseCase.execute(
                    CreateAgendaItemsUseCase.Params(
                        agendaDate,
                        it,
                        AgendaReducer.ITEMS_BEFORE_COUNT,
                        AgendaReducer.ITEMS_AFTER_COUNT
                    )
                )
                dispatch(
                    DataLoadedAction.AgendaItemsChanged(
                        start = start,
                        end = end,
                        agendaItems = agendaItems,
                        currentAgendaItemDate = if (changeCurrentAgendaItem && isFirstData) agendaDate else null
                    )
                )
                isFirstData = false
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
            || action is AgendaAction.LoadBefore
            || action is AgendaAction.LoadAfter
            || action is ScheduleAction.ScheduleChangeDate
            || action is CalendarAction.SwipeChangeDate

}

class LoadAllDataSideEffect : AppSideEffect() {

    private val playerRepository by required { playerRepository }
    private val questRepository by required { questRepository }
    private val repeatingQuestRepository by required { repeatingQuestRepository }
    private val findQuestsForRepeatingQuestUseCase by required { saveQuestsForRepeatingQuestUseCase }
    private val findNextDateForRepeatingQuestUseCase by required { findNextDateForRepeatingQuestUseCase }
    private val findPeriodProgressForRepeatingQuestUseCase by required { findPeriodProgressForRepeatingQuestUseCase }

    private var playerChannel: ReceiveChannel<Player?>? = null
    private var todayQuestsChannel: ReceiveChannel<List<Quest>>? = null
    private var repeatingQuestsChannel: ReceiveChannel<List<RepeatingQuest>>? = null

    override suspend fun doExecute(action: Action, state: AppState) {

        if (action is LoadDataAction.ChangePlayer) {
            playerChannel?.cancel()
            todayQuestsChannel?.cancel()
            repeatingQuestsChannel?.cancel()

            playerChannel = null
            todayQuestsChannel = null
            repeatingQuestsChannel = null

            playerRepository.purge(action.oldPlayerId)
            listenForPlayer()
            listenForQuests(state)
        }

        if (action == LoadDataAction.All) {
            listenForPlayer()
            listenForQuests(state)
            listenForRepeatingQuests()
        }
    }

    private fun listenForRepeatingQuests() {
        launch(UI) {
            repeatingQuestsChannel?.cancel()
            repeatingQuestsChannel = repeatingQuestRepository.listenForAll()
            repeatingQuestsChannel!!.consumeEach {
                launch {
                    val rqs = it
                        .map {
                            findNextDateForRepeatingQuestUseCase.execute(
                                FindNextDateForRepeatingQuestUseCase.Params(it)
                            )
                        }.map {
                            findPeriodProgressForRepeatingQuestUseCase.execute(
                                FindPeriodProgressForRepeatingQuestUseCase.Params(it)
                            )
                        }
//
//                    val scheduledQuests = rqs.map {
//                        saveQuestsForRepeatingQuestUseCase.execute(
//                            SaveQuestsForRepeatingQuestUseCase.Params(
//                                it,
//                                LocalDate.now(),
//                                LocalDate.now()
//                            )
//                        ).quests
//                    }
//
//                    scheduledQuests.forEach {
//                        it.forEach {
//                            Timber.d("AAA ${it.scheduledDate}")
//                        }
//
//                    }
                    dispatch(DataLoadedAction.RepeatingQuestsChanged(rqs))
                }
            }
        }
    }

    private fun listenForQuests(
        state: AppState
    ) {
        launch(UI) {
            todayQuestsChannel?.cancel()
            todayQuestsChannel = questRepository.listenForScheduledAt(state.dataState.today)
            todayQuestsChannel!!.consumeEach {
                updateWidgets()
                dispatch(DataLoadedAction.TodayQuestsChanged(it))
            }
        }
    }

    private fun updateWidgets() {
        AppWidgetUtil.updateAgendaWidget(myPoliApp.instance)
    }

    private fun listenForPlayer() {
        launch(UI) {
            playerChannel?.cancel()
            playerChannel = playerRepository.listen()
            playerChannel!!.consumeEach {
                dispatch(PlayerChanged(it!!))
            }
        }
    }

    override fun canHandle(action: Action) =
        action == LoadDataAction.All
            || action is LoadDataAction.ChangePlayer
}