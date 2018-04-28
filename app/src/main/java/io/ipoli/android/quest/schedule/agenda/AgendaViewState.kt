package io.ipoli.android.quest.schedule.agenda

import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.ionicons_typeface_library.Ionicons
import io.ipoli.android.R
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.datetime.isBetween
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.common.text.DateFormatter
import io.ipoli.android.common.view.AndroidColor
import io.ipoli.android.common.view.AndroidIcon
import io.ipoli.android.event.Event
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.schedule.agenda.usecase.CreateAgendaItemsUseCase.AgendaItem
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */

sealed class AgendaAction : Action {
    data class Load(val startDate: LocalDate) : AgendaAction()
    data class LoadBefore(val itemPosition: Int) : AgendaAction()
    data class LoadAfter(val itemPosition: Int) : AgendaAction()
    data class CompleteQuest(val itemPosition: Int) : AgendaAction()
    data class UndoCompleteQuest(val itemPosition: Int) : AgendaAction()
    data class FirstVisibleItemChanged(val itemPosition: Int) : AgendaAction()
}

object AgendaReducer : BaseViewStateReducer<AgendaViewState>() {

    override val stateKey = key<AgendaViewState>()

    override fun reduce(
        state: AppState,
        subState: AgendaViewState,
        action: Action
    ): AgendaViewState {
        return when (action) {

            is DataLoadedAction.AgendaItemsChanged -> {
                subState.copy(
                    type = AgendaViewState.StateType.DATA_CHANGED,
                    agendaItems = action.agendaItems,
                    scrollToPosition = findItemPositionToScrollTo(
                        action.currentAgendaItemDate,
                        action.agendaItems
                    )
                )
            }
            is AgendaAction.LoadBefore -> {
                subState.copy(
                    type = AgendaViewState.StateType.SHOW_TOP_LOADER
                )
            }
            is AgendaAction.LoadAfter -> {
                subState.copy(
                    type = AgendaViewState.StateType.SHOW_BOTTOM_LOADER
                )
            }
            is AgendaAction.FirstVisibleItemChanged -> {
                subState.copy(
                    type = AgendaViewState.StateType.IDLE
                )
            }
            else -> subState

        }
    }

    private fun findItemPositionToScrollTo(
        date: LocalDate?,
        agendaItems: List<AgendaItem>
    ) = date?.let {
        val currentAgendaItemDate = it
        val index = agendaItems.indexOfLast {
            when (it) {
                is AgendaItem.Date ->
                    it.startDate() == currentAgendaItemDate
                is AgendaItem.Week ->
                    currentAgendaItemDate.isBetween(
                        it.start,
                        it.end
                    )
                else -> false
            }
        }
        if (index < 0) null
        else index
    }

    override fun defaultState() = AgendaViewState(
        type = AgendaViewState.StateType.LOADING,
        agendaItems = listOf(),
        scrollToPosition = null
    )

    const val ITEMS_BEFORE_COUNT = 25
    const val ITEMS_AFTER_COUNT = 35
}

data class AgendaViewState(
    val type: AgendaViewState.StateType,
    val scrollToPosition: Int?,
    val agendaItems: List<AgendaItem>
) : ViewState {

    enum class StateType {
        LOADING,
        DATA_CHANGED,
        SHOW_TOP_LOADER,
        SHOW_BOTTOM_LOADER,
        IDLE
    }

}

fun AgendaViewState.toAgendaItemViewModels() =
    agendaItems.mapIndexed { index, item ->
        toAgendaViewModel(
            item,
            if (agendaItems.lastIndex >= index + 1) agendaItems[index + 1] else null
        )
    }

private fun toAgendaViewModel(
    agendaItem: AgendaItem,
    nextAgendaItem: AgendaItem? = null
): AgendaViewController.AgendaViewModel {

    return when (agendaItem) {
        is AgendaItem.QuestItem -> {
            val quest = agendaItem.quest
            val color = if (quest.isCompleted)
                R.color.md_grey_500
            else
                AndroidColor.valueOf(quest.color.name).color500

            AgendaViewController.QuestViewModel(
                id = quest.id,
                name = quest.name,
                tags = quest.tags.map {
                    AgendaViewController.TagViewModel(
                        it.name,
                        AndroidColor.valueOf(it.color.name).color500
                    )
                },
                startTime = formatStartTime(quest),
                color = color,
                icon = quest.icon?.let { AndroidIcon.valueOf(it.name).icon }
                        ?: Ionicons.Icon.ion_android_clipboard,
                isCompleted = quest.isCompleted,
                showDivider = shouldShowDivider(nextAgendaItem),
                isRepeating = quest.isFromRepeatingQuest,
                isFromChallenge = quest.isFromChallenge,
                isPlaceholder = quest.id.isEmpty()
            )
        }

        is AgendaItem.EventItem -> {
            val event = agendaItem.event

            AgendaViewController.EventViewModel(
                name = event.name,
                startTime = formatStartTime(event),
                color = event.color,
                icon = GoogleMaterial.Icon.gmd_event_available,
                showDivider = shouldShowDivider(nextAgendaItem)
            )
        }

        is AgendaItem.Date -> {
            val date = agendaItem.date
            val dayOfMonth = date.dayOfMonth
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .toUpperCase()
            AgendaViewController.DateHeaderViewModel("$dayOfMonth $dayOfWeek")
        }
        is AgendaItem.Week -> {
            val start = agendaItem.start
            val end = agendaItem.end
            val label = if (start.month != end.month) {
                "${DateFormatter.formatDayWithWeek(start)} - ${DateFormatter.formatDayWithWeek(
                    end
                )}"
            } else {
                "${start.dayOfMonth} - ${DateFormatter.formatDayWithWeek(end)}"
            }

            AgendaViewController.WeekHeaderViewModel(label)
        }
        is AgendaItem.Month -> {
            AgendaViewController.MonthDividerViewModel(
                monthToImage[agendaItem.month.month]!!,
                agendaItem.month.format(
                    DateTimeFormatter.ofPattern("MMMM yyyy")
                )
            )
        }
    }
}

private fun shouldShowDivider(nextAgendaItem: AgendaItem?) =
    !(nextAgendaItem == null || (nextAgendaItem !is AgendaItem.QuestItem && nextAgendaItem !is AgendaItem.EventItem))

private fun formatStartTime(quest: Quest): String {
    val start = quest.startTime ?: return "Unscheduled"
    val end = start.plus(quest.actualDuration.asMinutes.intValue)
    return "$start - $end"
}

private fun formatStartTime(event: Event): String {
    val start = event.startTime
    val end = start.plus(event.duration.intValue)
    return "$start - $end"
}

private val monthToImage = mapOf(
    Month.JANUARY to R.drawable.agenda_january,
    Month.FEBRUARY to R.drawable.agenda_february,
    Month.MARCH to R.drawable.agenda_march,
    Month.APRIL to R.drawable.agenda_april,
    Month.MAY to R.drawable.agenda_may,
    Month.JUNE to R.drawable.agenda_june,
    Month.JULY to R.drawable.agenda_july,
    Month.AUGUST to R.drawable.agenda_august,
    Month.SEPTEMBER to R.drawable.agenda_september,
    Month.OCTOBER to R.drawable.agenda_october,
    Month.NOVEMBER to R.drawable.agenda_november,
    Month.DECEMBER to R.drawable.agenda_december

)