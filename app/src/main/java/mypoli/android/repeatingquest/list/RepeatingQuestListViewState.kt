package mypoli.android.repeatingquest.list

import com.mikepenz.ionicons_typeface_library.Ionicons
import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
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

fun RepeatingQuestListViewState.toViewModels() =
    repeatingQuests.map {

        RepeatingQuestListViewController.RepeatingQuestViewModel(
            name = it.name,
            icon = it.icon?.let { AndroidIcon.valueOf(it.name).icon }
                ?: Ionicons.Icon.ion_android_clipboard,
            color = AndroidColor.valueOf(it.color.name).color500,
            next = "Next: Today",
            completedCount = 2,
            allCount = 3
        )
    }