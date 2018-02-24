package mypoli.android.repeatingquest.list

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
            it.name,
            AndroidIcon.BUS.icon,
            AndroidColor.GREEN.color500,
            "Next: Today",
            2,
            3
        )
    }
//    return listOf(
//        RepeatingQuestListViewController.RepeatingQuestViewModel(
//            "Workout",
//            AndroidIcon.BUS.icon,
//            AndroidColor.GREEN.color500,
//            "Next: Today",
//            2,
//            3
//        ),
//        RepeatingQuestListViewController.RepeatingQuestViewModel(
//            "Run",
//            AndroidIcon.BIKE.icon,
//            AndroidColor.BLUE.color500,
//            "Next: Tomorrow",
//            1,
//            5
//        ),
//        RepeatingQuestListViewController.RepeatingQuestViewModel(
//            "Cook",
//            AndroidIcon.ACADEMIC.icon,
//            AndroidColor.DEEP_ORANGE.color500,
//            "Next: Today",
//            4,
//            10
//        )
//    )