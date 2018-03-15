package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 2/23/18.
 */
class SaveRepeatingQuestUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository,
    private val saveQuestsForRepeatingQuestUseCase: SaveQuestsForRepeatingQuestUseCase
) : UseCase<SaveRepeatingQuestUseCase.Params, RepeatingQuest> {

    override fun execute(parameters: Params): RepeatingQuest {

        require(parameters.name.isNotBlank())

        val repeatingQuest = if (parameters.id.isEmpty()) {
            RepeatingQuest(
                name = parameters.name.trim(),
                icon = parameters.icon,
                color = parameters.color,
                category = parameters.category,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder,
                repeatingPattern = parameters.repeatingPattern,
                challengeId = parameters.challengeId
            )
        } else {

            val repeatingQuest = repeatingQuestRepository.findById(parameters.id)!!

            repeatingQuest.copy(
                name = parameters.name.trim(),
                icon = parameters.icon,
                color = parameters.color,
                category = parameters.category,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder,
                repeatingPattern = parameters.repeatingPattern,
                challengeId = parameters.challengeId
            )
        }

        val rq = if (parameters.id.isEmpty()) {
            // we should wait for updating the RepeatingPattern and then save the Repeating Quest
            saveQuestsFor(
                repeatingQuest.copy(
                    id = repeatingQuestRepository.generateId(),
                    createdAt = Instant.now()
                )
            )
        } else {
            questRepository.purgeAllNotCompletedForRepeating(repeatingQuest.id, LocalDate.now())
            saveQuestsFor(repeatingQuest)
        }

        return repeatingQuestRepository.save(rq)
    }

    private fun saveQuestsFor(repeatingQuest: RepeatingQuest): RepeatingQuest {
        val currentPeriod = repeatingQuest.repeatingPattern.periodRangeFor(LocalDate.now())
        val nextPeriodFirstDate = currentPeriod.end.plusDays(1)
        val end = repeatingQuest.repeatingPattern.periodRangeFor(nextPeriodFirstDate).end
        return saveQuestsForRepeatingQuestUseCase.execute(
            SaveQuestsForRepeatingQuestUseCase.Params(
                repeatingQuest = repeatingQuest,
                start = LocalDate.now(),
                end = end
            )
        ).repeatingQuest
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
        val repeatingPattern: RepeatingPattern,
        val challengeId: String? = null
    )
}