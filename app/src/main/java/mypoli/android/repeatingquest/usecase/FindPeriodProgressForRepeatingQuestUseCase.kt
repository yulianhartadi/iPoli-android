package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.PeriodProgress
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/26/18.
 */
class FindPeriodProgressForRepeatingQuestUseCase(
    private val questRepository: QuestRepository
) : UseCase<FindPeriodProgressForRepeatingQuestUseCase.Params, RepeatingQuest> {

    override fun execute(parameters: Params): RepeatingQuest {
        val rq = parameters.repeatingQuest
        val periodRange = rq.repeatingPattern.periodRangeFor(parameters.currentDate)
        return rq.copy(
            periodProgress = PeriodProgress(
                completedCount = questRepository.findCompletedCountForRepeatingQuestInPeriod(
                    rq.id,
                    periodRange.start,
                    periodRange.end
                ),
                allCount = rq.repeatingPattern.periodCount
            )
        )
    }

    data class Params(
        val repeatingQuest: RepeatingQuest,
        val currentDate: LocalDate = LocalDate.now()
    )
}