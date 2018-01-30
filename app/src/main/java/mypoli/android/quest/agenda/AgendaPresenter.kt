package mypoli.android.quest.agenda

import android.content.Context
import com.mikepenz.ionicons_typeface_library.Ionicons
import mypoli.android.R
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.quest.Quest
import mypoli.android.quest.agenda.usecase.CreateAgendaItemsUseCase
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.TextStyle
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */
class AgendaPresenter : AndroidStatePresenter<AppState, AgendaViewState> {
    override fun present(state: AppState, context: Context): AgendaViewState {

        return AgendaViewState(
            AgendaState.StateType.DATA_CHANGED,
            state.agendaState.agendaItems.map {
                toAgendaViewModel(it, context)
            },
            scrollToPosition = state.agendaState.scrollToPosition
        )
    }

    private fun toAgendaViewModel(
        agendaItem: CreateAgendaItemsUseCase.AgendaItem,
        context: Context
    ): AgendaViewController.AgendaViewModel {

        return when (agendaItem) {
            is CreateAgendaItemsUseCase.AgendaItem.QuestItem -> {
                val quest = agendaItem.quest
                AgendaViewController.QuestViewModel(
                    quest.name,
                    formatStartTime(quest),
                    AndroidColor.valueOf(quest.color.name).color500,
                    quest.icon?.let { AndroidIcon.valueOf(it.name).icon }
                        ?: Ionicons.Icon.ion_android_done
                )
            }
            is CreateAgendaItemsUseCase.AgendaItem.Date -> {
                val date = agendaItem.date
                val dayOfMonth = date.dayOfMonth
                val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).toUpperCase()
                AgendaViewController.DateHeaderViewModel("$dayOfMonth $dayOfWeek")
            }
            is CreateAgendaItemsUseCase.AgendaItem.Week -> {
                AgendaViewController.WeekHeaderViewModel(
                    "${DateFormatter.format(context, agendaItem.start)} - ${DateFormatter.format(context, agendaItem.end)}"
                )
            }
            is CreateAgendaItemsUseCase.AgendaItem.Month -> {
                AgendaViewController.MonthDividerViewModel(
                    R.drawable.challenge_category_build_skill_image, agendaItem.month.format(
                        DateTimeFormatter.ofPattern("MMMM yyyy")
                    )
                )
            }
        }

    }

    private fun formatStartTime(quest: Quest): String {
        val start = quest.actualStartTime ?: return "Unscheduled"
        val end = start.plus(quest.actualDuration.asMinutes.intValue)
        return "$start - $end"

    }

}