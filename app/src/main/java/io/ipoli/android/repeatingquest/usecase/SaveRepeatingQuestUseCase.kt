package io.ipoli.android.repeatingquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.*
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.repeatingquest.entity.RepeatingPattern
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository
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
                subQuests = createSubQuests(parameters.subQuestNames),
                challengeId = parameters.challengeId,
                note = parameters.note
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
                subQuests = createSubQuests(parameters.subQuestNames),
                challengeId = parameters.challengeId,
                note = parameters.note
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

    private fun createSubQuests(subQuestNames: List<String>) =
        subQuestNames
            .filter { it.isNotBlank() }
            .map {
                SubQuest(
                    name = it.trim(),
                    completedAtDate = null,
                    completedAtTime = null
                )
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
        val subQuestNames: List<String>,
        val color: Color,
        val icon: Icon? = null,
        val category: Category,
        val startTime: Time? = null,
        val duration: Int,
        val reminder: Reminder? = null,
        val repeatingPattern: RepeatingPattern,
        val challengeId: String? = null,
        val note: String? = null
    )
}