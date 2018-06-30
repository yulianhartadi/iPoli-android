package io.ipoli.android.repeatingquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/25/18.
 */
class FindNextDateForRepeatingQuestUseCase(
    private val questRepository: QuestRepository
) : UseCase<FindNextDateForRepeatingQuestUseCase.Params, RepeatingQuest> {

    override fun execute(parameters: Params): RepeatingQuest {
        val fromDate = parameters.fromDate
        val rq = parameters.repeatingQuest
        val nextScheduled =
            questRepository.findNextScheduledNotCompletedForRepeatingQuest(rq.id, fromDate)

        if (nextScheduled != null) {
            val patternNext = rq.repeatPattern.nextDate(fromDate)
            if (patternNext == null) {
                return rq.copy(
                    nextDate = nextScheduled.scheduledDate
                )
            }

            if (patternNext.isAfter(nextScheduled.scheduledDate) || patternNext.isEqual(
                    nextScheduled.scheduledDate
                )) {
                return rq.copy(
                    nextDate = nextScheduled.scheduledDate
                )
            }

            var nextDate = patternNext

            while (nextDate!!.isBefore(nextScheduled.scheduledDate)) {

                val originalScheduled =
                    questRepository.findOriginalScheduledForRepeatingQuestAtDate(rq.id, nextDate)
                if (originalScheduled == null) {
                    return rq.copy(
                        nextDate = nextDate
                    )
                }
                nextDate = rq.repeatPattern.nextDate(nextDate.plusDays(1))

                if (nextDate == null) {
                    return rq.copy(
                        nextDate = nextScheduled.scheduledDate
                    )
                }
            }

            return rq.copy(
                nextDate = nextScheduled.scheduledDate
            )
        } else {
            var nextDate: LocalDate? = rq.repeatPattern.nextDate(fromDate)
            if (nextDate == null) {
                return rq.copy(
                    nextDate = null
                )
            }
            while (true) {

                val originalScheduled =
                    questRepository.findOriginalScheduledForRepeatingQuestAtDate(rq.id, nextDate!!)
                if (originalScheduled == null) {
                    return rq.copy(
                        nextDate = nextDate
                    )
                }
                nextDate = rq.repeatPattern.nextDate(nextDate.plusDays(1))

                if (nextDate == null) {
                    return rq.copy(
                        nextDate = null
                    )
                }
            }
        }
    }

    data class Params(val repeatingQuest: RepeatingQuest, val fromDate: LocalDate = LocalDate.now())
}