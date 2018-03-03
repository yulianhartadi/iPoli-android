package mypoli.android.repeatingquest.edit.picker

import android.support.annotation.DrawableRes
import mypoli.android.common.AppState
import mypoli.android.common.BaseViewStateReducer
import mypoli.android.common.mvi.ViewState
import mypoli.android.common.redux.Action
import mypoli.android.pet.AndroidPetAvatar
import mypoli.android.repeatingquest.edit.picker.RepeatingPatternViewState.StateType.*
import mypoli.android.repeatingquest.entity.RepeatType
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.repeatType
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/21/18.
 */
sealed class RepeatingPatternAction : Action {
    data class LoadData(val repeatingPattern: RepeatingPattern?) : RepeatingPatternAction()
    data class ChangeFrequency(val index: Int) : RepeatingPatternAction()
    data class ToggleWeekDay(val weekDay: DayOfWeek) : RepeatingPatternAction()
    data class ChangeWeekDayCount(val index: Int) : RepeatingPatternAction()
    data class ToggleMonthDay(val day: Int) : RepeatingPatternAction()
    data class ChangeMonthDayCount(val index: Int) : RepeatingPatternAction()
    data class ChangeDayOfYear(val date: LocalDate) : RepeatingPatternAction()
    data class ChangeStartDate(val date: LocalDate) : RepeatingPatternAction()
    data class ChangeEndDate(val date: LocalDate) : RepeatingPatternAction()
    object CreatePattern : RepeatingPatternAction()
}


object RepeatingPatternReducer : BaseViewStateReducer<RepeatingPatternViewState>() {

    override val stateKey = key<RepeatingPatternViewState>()

    override fun reduce(
        state: AppState,
        subState: RepeatingPatternViewState,
        action: Action
    ): RepeatingPatternViewState {
        return when (action) {
            is RepeatingPatternAction.LoadData -> {
                val pattern = action.repeatingPattern
                val repeatType =
                    if (pattern != null) pattern.repeatType
                    else subState.repeatType

                val selectedWeekDays =
                    if (pattern != null) selectedWeekDaysFor(pattern) else subState.selectedWeekDays
                val weekDaysCount = Math.max(1, selectedWeekDays.size)
                val weekDaysCountIndex = subState.weekCountValues.indexOfFirst { weekDaysCount == it }

                val selectedMonthDays =
                    if (pattern != null) selectedMonthDaysFor(pattern) else subState.selectedMonthDays
                val monthDaysCount = Math.max(1, selectedMonthDays.size)
                val monthDaysCountIndex = subState.monthCountValues.indexOfFirst { monthDaysCount == it }

                val startDate = pattern?.start ?: subState.startDate
                val endDate = pattern?.end ?: subState.endDate

                val petAvatar = AndroidPetAvatar.valueOf(state.dataState.player!!.pet.avatar.name)

                subState.copy(
                    type = DATA_LOADED,
                    repeatType = repeatType,
                    repeatTypeIndex = repeatTypeIndexFor(repeatType),
                    weekDaysCountIndex = weekDaysCountIndex,
                    monthDaysCountIndex = monthDaysCountIndex,
                    selectedWeekDays = selectedWeekDays,
                    selectedMonthDays = selectedMonthDays,
                    isFlexible = isFlexible(repeatType, subState),
                    startDate = startDate,
                    endDate = endDate,
                    pickerEndDate = endDate ?: subState.pickerEndDate,
                    petAvatar = petAvatar.headImage
                )
            }

            is RepeatingPatternAction.ChangeFrequency -> {
                val repeatType = repeatTypeForIndex(action.index)
                subState.copy(
                    type = REPEAT_TYPE_CHANGED,
                    repeatType = repeatType,
                    repeatTypeIndex = repeatTypeIndexFor(repeatType),
                    isFlexible = isFlexible(repeatType, subState)
                )
            }

            is RepeatingPatternAction.ToggleWeekDay -> {
                val weekDay = action.weekDay
                val selectedWeekDays = if (subState.selectedWeekDays.contains(weekDay)) {
                    subState.selectedWeekDays.minus(weekDay)
                } else {
                    subState.selectedWeekDays.plus(weekDay)
                }

                subState.copy(
                    type = WEEK_DAYS_CHANGED,
                    selectedWeekDays = selectedWeekDays,
                    isFlexible = subState.weekDaysCount != selectedWeekDays.size
                )
            }

            is RepeatingPatternAction.ChangeWeekDayCount -> {
                subState.copy(
                    type = COUNT_CHANGED,
                    weekDaysCountIndex = action.index,
                    isFlexible = subState.weekCountValues[action.index] != subState.selectedWeekDays.size
                )
            }

            is RepeatingPatternAction.ToggleMonthDay -> {
                val day = action.day
                val selectedMonthDays = if (subState.selectedMonthDays.contains(day)) {
                    subState.selectedMonthDays.minus(day)
                } else {
                    subState.selectedMonthDays.plus(day)
                }
                subState.copy(
                    type = MONTH_DAYS_CHANGED,
                    selectedMonthDays = selectedMonthDays,
                    isFlexible = subState.monthDaysCount != selectedMonthDays.size
                )
            }

            is RepeatingPatternAction.ChangeMonthDayCount -> {
                subState.copy(
                    type = COUNT_CHANGED,
                    monthDaysCountIndex = action.index,
                    isFlexible = subState.monthCountValues[action.index] != subState.selectedMonthDays.size
                )
            }

            is RepeatingPatternAction.ChangeDayOfYear -> {
                subState.copy(
                    type = YEAR_DAY_CHANGED,
                    dayOfYear = action.date
                )
            }

            is RepeatingPatternAction.ChangeStartDate -> {
                subState.copy(
                    type = START_DATE_CHANGED,
                    startDate = action.date,
                    pickerEndDate = if (subState.endDate == null)
                        action.date.plusDays(1)
                    else subState.pickerEndDate
                )
            }

            is RepeatingPatternAction.ChangeEndDate -> {
                subState.copy(
                    type = END_DATE_CHANGED,
                    endDate = action.date,
                    pickerEndDate = action.date
                )
            }

            RepeatingPatternAction.CreatePattern -> {
                subState.copy(
                    type = PATTERN_CREATED,
                    resultPattern = createRepeatingPattern(subState)
                )
            }

            else -> {
                subState
            }
        }
    }

    private fun createRepeatingPattern(state: RepeatingPatternViewState): RepeatingPattern =
        when (state.repeatType) {
            RepeatType.DAILY -> {
                RepeatingPattern.Daily(state.startDate, state.endDate)
            }
            RepeatType.WEEKLY -> {
                if (state.isFlexible) {
                    RepeatingPattern.Flexible.Weekly(
                        timesPerWeek = state.weekCountValues[state.weekDaysCountIndex],
                        preferredDays = state.selectedWeekDays,
                        start = state.startDate,
                        end = state.endDate
                    )
                } else {
                    RepeatingPattern.Weekly(
                        daysOfWeek = state.selectedWeekDays,
                        start = state.startDate,
                        end = state.endDate
                    )
                }
            }
            RepeatType.MONTHLY -> {
                if (state.isFlexible) {
                    RepeatingPattern.Flexible.Monthly(
                        timesPerMonth = state.monthDaysCount,
                        preferredDays = state.selectedMonthDays,
                        start = state.startDate,
                        end = state.endDate
                    )
                } else {
                    RepeatingPattern.Monthly(
                        daysOfMonth = state.selectedMonthDays,
                        start = state.startDate,
                        end = state.endDate
                    )
                }
            }
            RepeatType.YEARLY -> {
                val date = state.dayOfYear
                RepeatingPattern.Yearly(
                    dayOfMonth = date.dayOfMonth,
                    month = date.month,
                    start = state.startDate,
                    end = state.endDate
                )
            }

        }

    private fun isFlexible(
        repeatType: RepeatType,
        state: RepeatingPatternViewState
    ) =
        when (repeatType) {
            RepeatType.DAILY -> false
            RepeatType.YEARLY -> false
            RepeatType.WEEKLY -> {
                val daysCount = state.weekCountValues[state.weekDaysCountIndex]
                daysCount != state.selectedWeekDays.size
            }
            RepeatType.MONTHLY -> {
                val daysCount = state.monthCountValues[state.monthDaysCountIndex]
                daysCount != state.selectedMonthDays.size
            }
        }

    private fun repeatTypeForIndex(index: Int) =
        RepeatType.values()[index]

    private fun repeatTypeIndexFor(repeatType: RepeatType) =
        repeatType.ordinal

    private fun selectedWeekDaysFor(pattern: RepeatingPattern): Set<DayOfWeek> =
        when (pattern) {
            is RepeatingPattern.Weekly -> pattern.daysOfWeek
            is RepeatingPattern.Flexible.Weekly -> pattern.preferredDays
            else -> setOf()
        }

    private fun selectedMonthDaysFor(pattern: RepeatingPattern): Set<Int> =
        when (pattern) {
            is RepeatingPattern.Monthly -> pattern.daysOfMonth
            is RepeatingPattern.Flexible.Monthly -> pattern.preferredDays
            else -> setOf()
        }

    override fun defaultState() =
        RepeatingPatternViewState(
            LOADING,
            RepeatType.WEEKLY,
            repeatTypeIndex = repeatTypeIndexFor(RepeatType.WEEKLY),
            weekDaysCountIndex = 0,
            monthDaysCountIndex = 0,
            selectedWeekDays = setOf(),
            selectedMonthDays = setOf(),
            weekCountValues = (1..6).toList(),
            monthCountValues = (1..31).toList(),
            isFlexible = true,
            dayOfYear = LocalDate.now(),
            startDate = LocalDate.now(),
            endDate = null,
            pickerEndDate = LocalDate.now(),
            resultPattern = null,
            petAvatar = null
        )

}

data class RepeatingPatternViewState(
    val type: RepeatingPatternViewState.StateType,
    val repeatType: RepeatType,
    val repeatTypeIndex: Int,
    val weekDaysCountIndex: Int,
    val monthDaysCountIndex: Int,
    val selectedWeekDays: Set<DayOfWeek>,
    val selectedMonthDays: Set<Int>,
    val weekCountValues: List<Int>,
    val monthCountValues: List<Int>,
    val isFlexible: Boolean,
    val dayOfYear: LocalDate,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val pickerEndDate: LocalDate,
    val resultPattern: RepeatingPattern?,
    @DrawableRes val petAvatar: Int?
    ) : ViewState {
    enum class StateType {
        LOADING,
        DATA_LOADED,
        REPEAT_TYPE_CHANGED,
        WEEK_DAYS_CHANGED,
        COUNT_CHANGED,
        MONTH_DAYS_CHANGED,
        YEAR_DAY_CHANGED,
        START_DATE_CHANGED,
        END_DATE_CHANGED,
        PATTERN_CREATED
    }

    val weekDaysCount
        get() = weekCountValues[weekDaysCountIndex]

    val monthDaysCount
        get() = monthCountValues[monthDaysCountIndex]
}