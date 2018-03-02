package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesBetween
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingQuest
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/2/18.
 */
class RepeatingQuestHistoryUseCase(private val questRepository: QuestRepository) :
    UseCase<RepeatingQuestHistoryUseCase.Params, Map<LocalDate, RepeatingQuestHistoryUseCase.QuestState>> {

    override fun execute(parameters: Params): Map<LocalDate, QuestState> {
        val start = parameters.start
        val end = parameters.end
        val rq = parameters.repeatingQuest
        require(!start.isAfter(end))

        val quests = questRepository.findCompletedForRepeatingQuestInPeriod(rq.id, start, end)
        val completedDates = quests.map { it.completedAtDate }

        return start.datesBetween(end).map {
            val shouldBeCompleted = rq.repeatingPattern.shouldScheduleOn(it)
            val state = if (shouldBeCompleted && completedDates.contains(it)) {
                QuestState.COMPLETED
            } else {
                QuestState.NOT_COMPLETED
            }
            it to state
        }.toMap()
    }

    data class Params(val repeatingQuest: RepeatingQuest, val start: LocalDate, val end: LocalDate)

    enum class QuestState {
        COMPLETED,
        NOT_COMPLETED,
        EMPTY,
        COMPLETED_NOT_ORIGINAL
    }

}