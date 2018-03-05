package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.datesBetween
import mypoli.android.common.datetime.isBeforeOrEqual
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/2/18.
 */
class CreateRepeatingQuestHistoryUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository
) :
    UseCase<CreateRepeatingQuestHistoryUseCase.Params, CreateRepeatingQuestHistoryUseCase.History> {

    override fun execute(parameters: Params): History {
        val start = parameters.start
        val end = parameters.end
        require(!start.isAfter(end))
        val rq = repeatingQuestRepository.findById(parameters.repeatingQuestId)
        requireNotNull(rq)

        val quests = questRepository.findCompletedForRepeatingQuestInPeriod(rq!!.id, start, end)
        val completedDates = quests.map { it.completedAtDate }.toSet()

        val data = start.datesBetween(end).map {
            val shouldBeCompleted = rq.repeatingPattern.shouldScheduleOn(it)
            val isCompleted = completedDates.contains(it)

            val state = when {
                shouldBeCompleted && isCompleted -> DateHistory.DONE_ON_SCHEDULE
                !shouldBeCompleted && isCompleted -> DateHistory.DONE_NOT_ON_SCHEDULE
                it.isBefore(rq.start) -> DateHistory.BEFORE_START
                rq.end != null && it.isAfter(rq.end) -> DateHistory.AFTER_END
                shouldBeCompleted && !isCompleted && it.isBeforeOrEqual(parameters.currentDate) -> DateHistory.FAILED
                it.isEqual(parameters.currentDate) -> DateHistory.TODAY
                it.isBefore(parameters.currentDate) -> DateHistory.SKIPPED
                shouldBeCompleted && !isCompleted -> DateHistory.TODO
                else -> DateHistory.EMPTY
            }
            it to state
        }.toMap()

        return History(
            currentDate = parameters.currentDate,
            start = start,
            end = end,
            data = data
        )
    }

    data class Params(
        val repeatingQuestId: String,
        val start: LocalDate,
        val end: LocalDate,
        val currentDate: LocalDate = LocalDate.now()
    )

    enum class DateHistory {
        DONE_ON_SCHEDULE,
        DONE_NOT_ON_SCHEDULE,
        SKIPPED,
        FAILED,
        TODAY,
        BEFORE_START,
        AFTER_END,
        TODO,
        EMPTY
    }

    data class History(
        val currentDate: LocalDate,
        val start: LocalDate,
        val end: LocalDate,
        val data: Map<LocalDate, DateHistory>
    )

}