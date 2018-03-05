package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.Time
import mypoli.android.quest.Category
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import mypoli.android.quest.Reminder
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/23/18.
 */
class SaveRepeatingQuestUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository,
    private val saveQuestsForRepeatingQuestUseCase: SaveQuestsForRepeatingQuestUseCase
) : UseCase<SaveRepeatingQuestUseCase.Params, RepeatingQuest> {

    override fun execute(parameters: Params): RepeatingQuest {

        require(parameters.name.isNotEmpty())

        val repeatingQuest = if (parameters.id.isEmpty()) {
            RepeatingQuest(
                name = parameters.name,
                icon = parameters.icon,
                color = parameters.color,
                category = parameters.category,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder,
                repeatingPattern = parameters.repeatingPattern
            )
        } else {

            val repeatingQuest = repeatingQuestRepository.findById(parameters.id)!!

            repeatingQuest.copy(
                name = parameters.name,
                icon = parameters.icon,
                color = parameters.color,
                category = parameters.category,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder,
                repeatingPattern = parameters.repeatingPattern
            )
        }

        val rq = repeatingQuestRepository.save(repeatingQuest)

        if (parameters.id.isEmpty()) {
            saveQuestsFor(rq)
        } else {
            questRepository.purgeAllNotCompletedForRepeating(rq.id, LocalDate.now())
            saveQuestsFor(rq)
        }

        return rq
    }

    private fun saveQuestsFor(repeatingQuest: RepeatingQuest) {
        val currentPeriod = repeatingQuest.repeatingPattern.periodRangeFor(LocalDate.now())
        val nextPeriodFirstDate = currentPeriod.end.plusDays(1)
        val end = repeatingQuest.repeatingPattern.periodRangeFor(nextPeriodFirstDate).end
        saveQuestsForRepeatingQuestUseCase.execute(
            SaveQuestsForRepeatingQuestUseCase.Params(
                repeatingQuest = repeatingQuest,
                start = LocalDate.now(),
                end = end
            )
        )
    }

    data class Params(
        val id: String = "",
        val name: String,
        val color: Color,
        val icon: Icon? = null,
        val category: Category,
        val startTime: Time? = null,
        val duration: Int,
        val reminder: Reminder? = null,
        val repeatingPattern: RepeatingPattern
    )
}