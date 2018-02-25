package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/25/18.
 */
class FindNextDateForRepeatingQuestUseCase(
    private val questRepository: QuestRepository
) : UseCase<FindNextDateForRepeatingQuestUseCase.Params, FindNextDateForRepeatingQuestUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val fromDate = parameters.fromDate
        val nextScheduled = questRepository.findNextScheduledForRepeatingQuest(fromDate)
        val nextOriginalScheduled =
            questRepository.findNextOriginalScheduledForRepeatingQuest(fromDate)

        val rq = parameters.repeatingQuest
        if (nextScheduled == null && nextOriginalScheduled == null) {
            val nextDate = when (rq.repeatingPattern) {
                is RepeatingPattern.Daily -> {
                    fromDate
                }
                else -> fromDate
            }
            return Result(
                rq.copy(
                    nextDate = nextDate
                )
            )
        }

        return Result(
            rq.copy(
                nextDate = nextScheduled!!.scheduledDate
            )
        )
    }

    data class Params(val repeatingQuest: RepeatingQuest, val fromDate: LocalDate)

    data class Result(val repeatingQuest: RepeatingQuest)

}