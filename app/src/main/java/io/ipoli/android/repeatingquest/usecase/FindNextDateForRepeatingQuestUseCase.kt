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

        return rq.copy(
            nextDate = nextScheduled?.let {
                it.scheduledDate!!
            })
    }

    data class Params(val repeatingQuest: RepeatingQuest, val fromDate: LocalDate = LocalDate.now())
}