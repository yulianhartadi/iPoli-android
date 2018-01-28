package mypoli.android.quest.agenda

import android.content.Context
import com.mikepenz.ionicons_typeface_library.Ionicons
import mypoli.android.common.AppState
import mypoli.android.common.redux.android.AndroidStatePresenter
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.quest.Quest
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 01/26/2018.
 */
class AgendaPresenter : AndroidStatePresenter<AppState, AgendaViewState> {
    override fun present(state: AppState, context: Context): AgendaViewState {
        val scheduledQuests = state.appDataState.scheduledQuests
        return AgendaViewState(
            AgendaState.StateType.DATA_CHANGED,
            LocalDate.now(),
            scheduledQuests.values.map { it.map { toQuestViewModel(it) } }.flatten()
        )
    }

    private fun toQuestViewModel(quest: Quest): AgendaViewController.QuestViewModel {
        return AgendaViewController.QuestViewModel(
            quest.name,
            formatStartTime(quest),
            AndroidColor.valueOf(quest.color.name).color500,
            quest.icon?.let { AndroidIcon.valueOf(it.name).icon } ?: Ionicons.Icon.ion_android_done
        )
    }

    private fun formatStartTime(quest: Quest): String {
        val start = quest.actualStartTime ?: return "Unscheduled"
        val end = start.plus(quest.actualDuration.asMinutes.intValue)
        return "$start - $end"

    }

}