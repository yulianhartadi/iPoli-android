package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/25/18.
 */
class FindNextDateForRepeatingQuestUseCase(
    private val questRepository: QuestRepository
) : UseCase<FindNextDateForRepeatingQuestUseCase.Params, RepeatingQuest> {

    override fun execute(parameters: Params): RepeatingQuest {
        val fromDate = parameters.fromDate
        val rq = parameters.repeatingQuest
        val nextScheduled = questRepository.findNextScheduledForRepeatingQuest(rq.id, fromDate)


        if (nextScheduled != null) {
            val patternNext = rq.repeatingPattern.nextDate(fromDate)
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
                nextDate = rq.repeatingPattern.nextDate(nextDate.plusDays(1))

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
            var nextDate: LocalDate? = rq.repeatingPattern.nextDate(fromDate)

            while (true) {

                val originalScheduled =
                    questRepository.findOriginalScheduledForRepeatingQuestAtDate(rq.id, nextDate!!)
                if (originalScheduled == null) {
                    return rq.copy(
                        nextDate = nextDate
                    )
                }
                nextDate = rq.repeatingPattern.nextDate(nextDate.plusDays(1))

                if (nextDate == null) {
                    return rq.copy(
                        nextDate = null
                    )
                }
            }
        }
    }

    data class Params(val repeatingQuest: RepeatingQuest, val fromDate: LocalDate)
}