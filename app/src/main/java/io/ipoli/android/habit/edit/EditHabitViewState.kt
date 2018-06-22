package io.ipoli.android.habit.edit

import io.ipoli.android.Constants
import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.AppState
import io.ipoli.android.common.BaseViewStateReducer
import io.ipoli.android.common.DataLoadedAction
import io.ipoli.android.common.Validator
import io.ipoli.android.common.mvi.BaseViewState
import io.ipoli.android.common.redux.Action
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.edit.EditHabitViewState.StateType.*
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import org.threeten.bp.DayOfWeek

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/16/18.
 */
sealed class EditHabitAction : Action {
    object Save : EditHabitAction()

    object MakeBad : EditHabitAction()
    object MakeGood : EditHabitAction()

    data class Load(
        val habitId: String,
        val params: EditHabitViewController.Params?
    ) : EditHabitAction()
    data class DeselectDay(val weekDay: DayOfWeek) : EditHabitAction()
    data class SelectDay(val weekDay: DayOfWeek) : EditHabitAction()
    data class AddTag(val tagName: String) : EditHabitAction()
    data class RemoveTag(val tag: Tag) : EditHabitAction()
    data class ChangeColor(val color: Color) : EditHabitAction()
    data class ChangeIcon(val icon: Icon) : EditHabitAction()
    data class ChangeChallenge(val challenge: Challenge?) : EditHabitAction()
    data class ChangeNote(val note: String) : EditHabitAction()
    data class Validate(
        val name: String,
        val selectedTimesADayPosition: Int
    ) : EditHabitAction()

    data class Remove(val habitId: String) : EditHabitAction()
}

object EditHabitReducer : BaseViewStateReducer<EditHabitViewState>() {

    override val stateKey = key<EditHabitViewState>()

    override fun reduce(
        state: AppState,
        subState: EditHabitViewState,
        action: Action
    ) =
        when (action) {
            is EditHabitAction.Load -> {
                val s = if (action.habitId.isBlank()) {
                    subState.copy(
                        type = EditHabitViewState.StateType.HABIT_DATA_CHANGED,
                        tags = state.dataState.tags,
                        isEditing = false
                    )
                } else {
                    createFromHabits(state.dataState.habits!!, action.habitId, subState, state)
                }
                val params = action.params
                if (params != null) {
                    s.copy(
                        name = params.name,
                        color = params.color,
                        icon = params.icon,
                        timesADay = params.timesADay,
                        isGood = params.isGood,
                        days = params.days
                    )
                } else s
            }

            is DataLoadedAction.TagsChanged ->
                subState.copy(
                    type = TAGS_CHANGED,
                    tags = action.tags - subState.habitTags
                )

            is DataLoadedAction.ChallengesChanged ->
                if (subState.isEditing && subState.challenge != null)
                    subState.copy(
                        type = CHALLENGE_CHANGED,
                        challenge = action.challenges.firstOrNull { it.id == subState.challenge.id }
                    )
                else
                    subState

            is EditHabitAction.SelectDay ->
                subState.copy(
    type = EditHabitViewState.StateType.DAYS_CHANGED,
                    days = subState.days + action.weekDay
                )

            is EditHabitAction.DeselectDay ->
                subState.copy(
                    type = EditHabitViewState.StateType.DAYS_CHANGED,
                    days = subState.days - action.weekDay
                )

            is EditHabitAction.RemoveTag -> {
                val habitTags = subState.habitTags - action.tag
                subState.copy(
                    type = TAGS_CHANGED,
                    habitTags = habitTags,
                    tags = subState.tags + action.tag,
                    maxTagsReached = habitTags.size >= Constants.MAX_TAGS_PER_ITEM
                )
            }

            is EditHabitAction.AddTag -> {
                val tag = subState.tags.first { it.name == action.tagName }
                val habitTags = subState.habitTags + tag
                subState.copy(
                    type = TAGS_CHANGED,
                    habitTags = habitTags,
                    tags = subState.tags - tag,
                    maxTagsReached = habitTags.size >= Constants.MAX_TAGS_PER_ITEM
                )
            }

            is EditHabitAction.ChangeColor ->
                subState.copy(
                    type = COLOR_CHANGED,
                    color = action.color
                )

            is EditHabitAction.ChangeIcon ->
                subState.copy(
                    type = ICON_CHANGED,
                    icon = action.icon
                )

            is EditHabitAction.ChangeChallenge ->
                subState.copy(
                    type = CHALLENGE_CHANGED,
                    challenge = action.challenge
                )

            is EditHabitAction.ChangeNote ->
                subState.copy(
                    type = NOTE_CHANGED,
                    note = action.note
                )

            is EditHabitAction.MakeGood ->
                subState.copy(
                    type = HABIT_TYPE_CHANGED,
                    isGood = true
                )

            is EditHabitAction.MakeBad ->
                subState.copy(
                    type = HABIT_TYPE_CHANGED,
                    isGood = false
                )

            is EditHabitAction.Validate -> {
                val name = action.name
                val errors = Validator.validate(action).check<ValidationError> {
                    "name" {
                        given { name.isBlank() } addError ValidationError.EMPTY_NAME
                    }
                    "days" {
                        given { subState.days.isEmpty() } addError ValidationError.EMPTY_DAYS
                    }
                }

                when {
                    errors.isEmpty() ->
                        subState.copy(
                            type = VALID_NAME,
                            name = name,
                            timesADay = subState.timesADayValues[action.selectedTimesADayPosition]
                        )
                    errors.contains(ValidationError.EMPTY_NAME) -> subState.copy(
                        type = VALIDATION_ERROR_EMPTY_NAME
                    )
                    else -> subState.copy(
                        type = VALIDATION_ERROR_EMPTY_DAYS
                    )
                }
            }

            else -> subState
        }

    private fun createFromHabits(
        habits: List<Habit>,
        habitId: String,
        subState: EditHabitViewState,
        state: AppState
    ): EditHabitViewState {
        val habit = habits.firstOrNull { it.id == habitId }

        return if (habit == null) {
            subState.copy(
                type = REMOVED
            )
        } else {
            subState.copy(
                type = HABIT_DATA_CHANGED,
                habit = habit,
                id = habit.id,
                name = habit.name,
                color = habit.color,
                icon = habit.icon,
                habitTags = habit.tags,
                tags = state.dataState.tags - habit.tags,
                note = habit.note,
                maxTagsReached = habit.tags.size >= Constants.MAX_TAGS_PER_ITEM,
                isGood = habit.isGood,
                timesADay = habit.timesADay,
                days = habit.days.toSet(),
                isEditing = true
            )
        }
    }

    override fun defaultState() = EditHabitViewState(
        type = LOADING,
        habit = null,
        id = "",
        name = "",
        habitTags = emptyList(),
        tags = emptyList(),
        days = DayOfWeek.values().toSet(),
        isGood = true,
        timesADay = 1,
        color = Color.GREEN,
        icon = Icon.FLOWER,
        challenge = null,
        note = "",
        maxTagsReached = false,
        timesADayValues = (1..8).toList(),
        isEditing = false
    )

    enum class ValidationError {
        EMPTY_NAME, EMPTY_DAYS
    }

}

data class EditHabitViewState(
    val type: StateType,
    val habit: Habit?,
    val id: String,
    val name: String,
    val habitTags: List<Tag>,
    val tags: List<Tag>,
    val days: Set<DayOfWeek>,
    val isGood: Boolean,
    val timesADay: Int,
    val color: Color,
    val icon: Icon,
    val challenge: Challenge?,
    val note: String,
    val maxTagsReached: Boolean,
    val timesADayValues: List<Int>,
    val isEditing: Boolean
) : BaseViewState() {
    enum class StateType {
        LOADING,
        HABIT_DATA_CHANGED,
        TAGS_CHANGED,
        DAYS_CHANGED,
        COLOR_CHANGED,
        ICON_CHANGED,
        CHALLENGE_CHANGED,
        NOTE_CHANGED,
        VALID_NAME,
        HABIT_TYPE_CHANGED,
        VALIDATION_ERROR_EMPTY_NAME,
        VALIDATION_ERROR_EMPTY_DAYS,
        REMOVED
    }
}