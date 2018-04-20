package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.Validator.Companion.validate
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.Reminder
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.ReminderScheduler
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.quest.usecase.Result.*
import io.ipoli.android.quest.usecase.Result.ValidationError.EMPTY_NAME
import io.ipoli.android.quest.usecase.Result.ValidationError.TIMER_RUNNING
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 9/30/17.
 */
sealed class Result {

    enum class ValidationError {
        EMPTY_NAME,
        TIMER_RUNNING
    }

    data class Added(val quest: Quest) : Result()
    data class Invalid(val error: ValidationError) : Result()
}

class SaveQuestUseCase(
    private val questRepository: QuestRepository,
    private val reminderScheduler: ReminderScheduler
) : UseCase<SaveQuestUseCase.Parameters, Result> {

    data class Parameters(
        val id: String = "",
        val name: String,
        val subQuests: List<SubQuest>?,
        val color: Color,
        val icon: Icon? = null,
        val startTime: Time? = null,
        val scheduledDate: LocalDate?,
        val duration: Int,
        val reminders: List<Reminder>?,
        val repeatingQuestId: String? = null,
        val challengeId: String? = null,
        val note: String = "",
        val tags: List<Tag>?
    )

    override fun execute(parameters: Parameters): Result {
        val errors = validate(parameters).check<ValidationError> {
            "name" {
                given { name.isBlank() } addError EMPTY_NAME
            }
        }

        if (errors.isNotEmpty()) {
            return Invalid(errors.first())
        }
        val quest = if (parameters.id.isEmpty()) {

            val subQuests = parameters.subQuests
                ?.filter { it.name.isNotBlank() } ?: listOf()

            Quest(
                name = parameters.name.trim(),
                icon = parameters.icon,
                color = parameters.color,
                scheduledDate = parameters.scheduledDate,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminders = parameters.reminders ?: emptyList(),
                subQuests = subQuests,
                repeatingQuestId = parameters.repeatingQuestId,
                challengeId = parameters.challengeId,
                note = parameters.note,
                tags = parameters.tags ?: emptyList()
            )
        } else {

            val quest = questRepository.findById(parameters.id)!!

            if (quest.isStarted) {
                return Invalid(TIMER_RUNNING)
            }

            val subQuests = parameters.subQuests
                ?.filter { it.name.isNotBlank() } ?: quest.subQuests

            val originalScheduledDate = quest.originalScheduledDate ?: parameters.scheduledDate

            quest.copy(
                name = parameters.name.trim(),
                icon = parameters.icon,
                color = parameters.color,
                scheduledDate = parameters.scheduledDate,
                originalScheduledDate = originalScheduledDate,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminders = parameters.reminders ?: quest.reminders,
                subQuests = subQuests,
                repeatingQuestId = parameters.repeatingQuestId,
                challengeId = parameters.challengeId,
                note = parameters.note,
                tags = parameters.tags ?: quest.tags
            )
        }

        questRepository.save(quest)

        val reminderTime = questRepository.findNextReminderTime()
        reminderTime?.let {
            reminderScheduler.schedule(it)
        }
        return Added(quest)
    }
}