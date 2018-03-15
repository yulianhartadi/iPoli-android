package mypoli.android.challenge.add

import mypoli.android.challenge.QuestPickerAction
import mypoli.android.challenge.QuestPickerViewState
import mypoli.android.challenge.add.AddChallengeViewState.StateType.*
import mypoli.android.challenge.entity.Challenge
import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.quest.BaseQuest
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/8/18.
 */
sealed class AddChallengeAction : Action {
    object Back : AddChallengeAction()
    object UpdateSummary : AddChallengeAction()
}

object AddChallengeReducer : BaseViewStateReducer<AddChallengeViewState> () {

    override val stateKey = key<AddChallengeViewState>()


    override fun reduce(
        state: AppState,
        subState: AddChallengeViewState,
        action: Action
    ) =
        when (action) {
            AddChallengeNameAction.Next -> {
                val s = state.stateFor(AddChallengeNameViewState::class.java)
                subState.copy(
                    type = CHANGE_PAGE,
                    adapterPosition = subState.adapterPosition + 1,
                    name = s.name,
                    color = s.color,
                    icon = s.icon,
                    difficulty = s.difficulty
                )
            }

            is AddChallengeNameAction.ChangeColor -> {
                subState.copy(
                    type = COLOR_CHANGED,
                    color = action.color
                )
            }

            is AddChallengeMotivationAction.Next -> {
                val s = state.stateFor(AddChallengeMotivationViewState::class.java)
                subState.copy(
                    type = CHANGE_PAGE,
                    adapterPosition = subState.adapterPosition + 1,
                    motivationList = listOf(s.motivation1, s.motivation2, s.motivation3)
                )
            }

            is AddChallengeEndDateAction.SelectDate -> {
                subState.copy(
                    type = CHANGE_PAGE,
                    adapterPosition = subState.adapterPosition + 1,
                    end = action.date
                )
            }

            is QuestPickerAction.Next -> {
                val s = state.stateFor(QuestPickerViewState::class.java)
                subState.copy(
                    type = CHANGE_PAGE,
                    adapterPosition = subState.adapterPosition + 1,
                    allQuests = s.allQuests.map {
                        it.baseQuest
                    },
                    selectedQuestIds = s.selectedQuests
                )
            }

            AddChallengeAction.Back -> {
                val adapterPosition = subState.adapterPosition - 1
                if (adapterPosition < 0) {
                    subState.copy(
                        type = CLOSE
                    )
                } else {
                    subState.copy(
                        type = CHANGE_PAGE,
                        adapterPosition = adapterPosition
                    )
                }
            }

            AddChallengeSummaryAction.Save ->
                subState.copy(
                    type = CLOSE
                )

            else -> subState
    }

    override fun defaultState() =
        AddChallengeViewState(
            type = INITIAL,
            adapterPosition = 0,
            name = "",
            color = Color.GREEN,
            icon = null,
            difficulty = Challenge.Difficulty.NORMAL,
            end = LocalDate.now(),
            motivationList = listOf(),
            allQuests = listOf(),
            selectedQuestIds = setOf()
        )
}


data class AddChallengeViewState(
    val type: AddChallengeViewState.StateType,
    val adapterPosition: Int,
    val name: String,
    val color: Color,
    val icon: Icon?,
    val difficulty: Challenge.Difficulty,
    val end: LocalDate,
    val motivationList: List<String>,
    val allQuests: List<BaseQuest>,
    val selectedQuestIds: Set<String>
) : ViewState {
    enum class StateType {
        INITIAL,
        DATA_CHANGED,
        CHANGE_PAGE,
        CLOSE,
        COLOR_CHANGED
    }
}