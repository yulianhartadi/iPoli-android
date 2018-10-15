package io.ipoli.android.habit.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.datesBetween
import io.ipoli.android.habit.data.Habit
import io.ipoli.android.habit.usecase.CreateHabitHistoryItemsUseCase.HabitHistoryItem.State.*
import io.ipoli.android.quest.Color
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/12/18.
 */
class CreateHabitHistoryItemsUseCase :
    UseCase<CreateHabitHistoryItemsUseCase.Params, List<CreateHabitHistoryItemsUseCase.HabitHistoryItem>> {

    override fun execute(parameters: Params): List<HabitHistoryItem> {
        val habit = parameters.habit
        val today = parameters.today
        val createdAt = DateUtils.fromMillis(habit.createdAt.toEpochMilli())

        val firstCompletedDate = habit.history.toSortedMap().entries.firstOrNull {
            it.value.completedCount > 0
        }?.key
        val firstDate = if (firstCompletedDate == null) createdAt else DateUtils.min(
            createdAt,
            firstCompletedDate
        )

        val completedIndexes = mutableListOf<Int>()
        var index = 0

        val items =
            parameters.startDate.minusDays(1)
                .datesBetween(parameters.endDate.plusDays(1)).map {
                    val isCompleted = habit.isCompletedForDate(it)
                    val isGood = habit.isGood

                    val state = when {
                        it > today || it < firstDate -> EMPTY
                        it == today && !habit.shouldBeDoneOn(it) && isGood && !isCompleted -> EMPTY
                        it == today && isGood && !isCompleted -> NOT_COMPLETED_TODAY
                        it == today && !isGood && isCompleted -> FAILED
                        isGood && isCompleted -> COMPLETED
                        !isGood && isCompleted -> FAILED
                        !habit.shouldBeDoneOn(it) -> EMPTY
                        isGood && !isCompleted -> FAILED
                        !isGood && !isCompleted -> COMPLETED
                        else -> EMPTY
                    }

                    if (state == COMPLETED) {
                        completedIndexes.add(index)
                    }
                    index++
                    HabitHistoryItem(
                        date = it,
                        isGood = habit.isGood,
                        completedCount = habit.completedCountForDate(it),
                        timesADay = habit.timesADay,
                        color = habit.color,
                        shouldBeDone = habit.days.contains(it.dayOfWeek),
                        state = state

                    )
                }

        val connectedIndexes = mutableListOf<Int>()

        completedIndexes.forEachIndexed { i, completedIndex ->
            if (i < completedIndexes.size - 1) {
                val nextIndex = completedIndexes[i + 1]

                val hasFailed =
                    items
                        .subList(completedIndex + 1, nextIndex)
                        .any { it.state == FAILED }

                if (!hasFailed) {
                    connectedIndexes.addAll((completedIndex + 1) until nextIndex)
                }

            }
        }

        return items.mapIndexedNotNull { i, item ->
            if (i == 0 || i == items.size - 1) null
            else {
                val isPreviousCompleted =
                    items[i - 1].state == COMPLETED || connectedIndexes.contains(i - 1)
                val isNextCompleted =
                    items[i + 1].state == COMPLETED || connectedIndexes.contains(i + 1)

                val shouldNotComplete = connectedIndexes.contains(i)
                item.copy(
                    isPreviousCompleted = isPreviousCompleted || shouldNotComplete,
                    isNextCompleted = isNextCompleted || shouldNotComplete,
                    state = if (item.state == EMPTY
                        && shouldNotComplete && item.date < today
                    ) CONNECTED
                    else item.state
                )
            }
        }
    }

    data class Params(
        val habit: Habit,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val today: LocalDate = LocalDate.now()
    )

    data class HabitHistoryItem(
        val date: LocalDate,
        val state: State,
        val isGood: Boolean,
        val completedCount: Int,
        val timesADay: Int,
        val color: Color,
        val shouldBeDone: Boolean,
        val isPreviousCompleted: Boolean = false,
        val isNextCompleted: Boolean = false
    ) {
        enum class State {
            FAILED, EMPTY, COMPLETED, NOT_COMPLETED_TODAY, CONNECTED
        }
    }
}