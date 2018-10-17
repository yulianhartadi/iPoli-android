package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.entity.Challenge.TrackedValue.Progress
import io.ipoli.android.challenge.entity.Challenge.TrackedValue.Progress.DayProgress.State
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.datesBetween
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/16/18.
 */
class CreateChallengeProgressItemsUseCase :
    UseCase<CreateChallengeProgressItemsUseCase.Params, List<Progress.DayProgress>> {

    override fun execute(parameters: Params): List<Progress.DayProgress> {
        val challenge = parameters.challenge
        val today = parameters.today
        val firstDate = DateUtils.fromMillis(challenge.createdAt.toEpochMilli())

        val completedIndexes = mutableListOf<Int>()
        var index = 0

        val items = parameters.startDate.datesBetween(parameters.endDate).map {
            var all = 0
            var completed = 0

            challenge.quests.forEach { q ->
                if (q.scheduledDate == it) {
                    all++
                    if (q.isCompleted) {
                        completed++
                    }
                }
            }
            challenge.habits.forEach { h ->
                val isCompleted = h.isCompletedForDate(it)
                if ((isCompleted && h.isGood) || (!isCompleted && !h.isGood)) completed++
                if (h.shouldBeDoneOn(it) || isCompleted) all++
            }

            val state = when {
                it < firstDate -> State.EMPTY
                all > 0 && all == completed -> State.COMPLETED
                it == today && all > 0 -> State.NOT_COMPLETED_TODAY
                all == 0 -> State.EMPTY
                completed < all && it < today-> State.FAILED
                else -> State.EMPTY
            }

            if (state == State.COMPLETED) {
                completedIndexes.add(index)
            }
            index++

            Progress.DayProgress(
                date = it,
                progress = (completed.toFloat() / all * 100).toInt(),
                color = challenge.color,
                shouldDoNothing = all == 0,
                isPreviousCompleted = Random().nextBoolean(),
                isNextCompleted = Random().nextBoolean(),
                state = state,
                isFirstDay = firstDate == it,
                isEndDay = challenge.endDate == it
            )
        }

        val connectedIndexes = mutableListOf<Int>()

        completedIndexes.forEachIndexed { i, completedIndex ->
            if (i < completedIndexes.size - 1) {
                val nextIndex = completedIndexes[i + 1]

                val hasFailed =
                    items
                        .subList(completedIndex + 1, nextIndex)
                        .any { it.state == State.FAILED }

                if (!hasFailed) {
                    connectedIndexes.addAll((completedIndex + 1) until nextIndex)
                }

            }
        }
        return items.mapIndexed { i, item ->
            val isPreviousCompleted =
                if (i == 0) false
                else items[i - 1].state == State.COMPLETED || connectedIndexes.contains(i - 1)

            val isNextCompleted =
                if (i == items.lastIndex) false
                else items[i + 1].state == State.COMPLETED || connectedIndexes.contains(i + 1)

            val shouldNotComplete = connectedIndexes.contains(i)
            item.copy(
                isPreviousCompleted = isPreviousCompleted || shouldNotComplete,
                isNextCompleted = isNextCompleted || shouldNotComplete,
                state = if (item.state == State.EMPTY
                    && shouldNotComplete && item.date < today
                ) State.CONNECTED
                else item.state
            )
        }
    }

    data class Params(
        val challenge: Challenge,
        val startDate: LocalDate,
        val endDate: LocalDate,
        val today: LocalDate = LocalDate.now()
    )
}