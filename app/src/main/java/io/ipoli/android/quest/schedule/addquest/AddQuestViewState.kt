package io.ipoli.android.quest.schedule.addquest

import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.common.mvi.ViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.schedule.agenda.AgendaReducer
import io.ipoli.android.quest.usecase.Result
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 11/2/17.
 */

sealed class AddQuestAction : Action {
    data class Save(val name: String) : AddQuestAction()
    data class DatePicked(val date: LocalDate?) : AddQuestAction()
    object DatePickerCanceled : AddQuestAction()
    data class TimePicked(val time: Time?) : AddQuestAction()
    data class ColorPicked(val color: Color) : AddQuestAction()
    data class IconPicked(val icon: Icon?) : AddQuestAction()
    data class Load(val date: LocalDate?) : AddQuestAction()
    object QuestSaved : AddQuestAction()
    data class SaveInvalidQuest(val error: Result.ValidationError) : AddQuestAction()
    data class DurationPicked(val minutes: Int) : AddQuestAction()
    data class TagsPicked(val tags: Set<Tag>) : AddQuestAction()
}

object AddQuestReducer : BaseViewStateReducer<AddQuestViewState>() {

    override val stateKey = AgendaReducer.key<AddQuestViewState>()

    override fun reduce(
        state: AppState,
        subState: AddQuestViewState,
        action: Action
    ) =
        when (action) {
            is AddQuestAction.Load ->
                subState.copy(
                    type = StateType.DATA_LOADED,
                    originalDate = action.date,
                    date = action.date
                )

            is AddQuestAction.DatePicked ->
                subState.copy(type = StateType.DATE_PICKED, date = action.date)

            AddQuestAction.DatePickerCanceled ->
                subState.copy(type = StateType.PICK_DATE_CANCELED)

            is AddQuestAction.TimePicked ->
                subState.copy(type = StateType.TIME_PICKED, time = action.time)

            is AddQuestAction.DurationPicked ->
                subState.copy(type = StateType.DURATION_PICKED, duration = action.minutes)

            is AddQuestAction.TagsPicked ->
                subState.copy(type = StateType.TAGS_PICKED, tags = action.tags)

            is AddQuestAction.ColorPicked ->
                subState.copy(type = StateType.COLOR_PICKED, color = action.color)

            is AddQuestAction.IconPicked ->
                subState.copy(type = StateType.ICON_PICKED, icon = action.icon)

            is AddQuestAction.SaveInvalidQuest -> {
                subState.copy(type = StateType.VALIDATION_ERROR_EMPTY_NAME)
            }

            AddQuestAction.QuestSaved -> {
                defaultState().copy(
                    type = StateType.QUEST_SAVED,
                    originalDate = subState.originalDate,
                    date = subState.date
                )
            }

            else -> subState
        }

    override fun defaultState() =
        AddQuestViewState(
            type = StateType.DATA_LOADED,
            originalDate = LocalDate.now(),
            date = null,
            color = null,
            duration = null,
            time = null,
            icon = null,
            tags = emptySet()
        )
}


data class AddQuestViewState(
    val type: StateType,
    val originalDate: LocalDate?,
    val date: LocalDate?,
    val time: Time?,
    val duration: Int?,
    val color: Color?,
    val icon: Icon?,
    val tags : Set<Tag>
) : ViewState

enum class StateType {
    DATA_LOADED,
    DATE_PICKED,
    TIME_PICKED,
    DURATION_PICKED,
    COLOR_PICKED,
    ICON_PICKED,
    TAGS_PICKED,
    VALIDATION_ERROR_EMPTY_NAME,
    QUEST_SAVED,
    PICK_DATE_CANCELED
}