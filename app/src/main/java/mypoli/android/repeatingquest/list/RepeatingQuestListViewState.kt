package mypoli.android.repeatingquest.list

import android.content.Context
import com.mikepenz.ionicons_typeface_library.Ionicons
import mypoli.android.R
import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.common.text.DateFormatter
import mypoli.android.common.view.AndroidColor
import mypoli.android.common.view.AndroidIcon
import mypoli.android.repeatingquest.entity.RepeatingQuest

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/14/18.
 */
sealed class RepeatingQuestListAction : Action {
    object LoadData : RepeatingQuestListAction()
}


object RepeatingQuestListReducer : BaseViewStateReducer<RepeatingQuestListViewState>() {

    override val stateKey = key<RepeatingQuestListViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingQuestListViewState,
        action: Action
    ) =
        when (action) {
            RepeatingQuestListAction.LoadData -> {
                subState.copy(
                    type = RepeatingQuestListViewState.StateType.DATA_LOADED,
                    repeatingQuests = state.dataState.repeatingQuests
                )
            }

            else -> subState
    }

    override fun defaultState() =
        RepeatingQuestListViewState(
            type = RepeatingQuestListViewState.StateType.LOADING,
            repeatingQuests = listOf()
        )

}

data class RepeatingQuestListViewState(
    val type: RepeatingQuestListViewState.StateType,
    val repeatingQuests: List<RepeatingQuest>
) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED
    }
}

fun RepeatingQuestListViewState.toViewModels(context: Context) =
    repeatingQuests.map {
        val next = if (it.nextDate != null) {
            context.getString(
                R.string.repeating_quest_next,
                DateFormatter.format(context, it.nextDate)
            )
        } else {
            context.getString(
                R.string.repeating_quest_next,
                context.getString(R.string.unscheduled)
            )
        }
        RepeatingQuestListViewController.RepeatingQuestViewModel(
            name = it.name,
            icon = it.icon?.let { AndroidIcon.valueOf(it.name).icon }
                ?: Ionicons.Icon.ion_android_clipboard,
            color = AndroidColor.valueOf(it.color.name).color500,
            next = next,
            completedCount = it.periodProgress!!.completedCount,
            allCount = it.periodProgress.allCount
        )
    }