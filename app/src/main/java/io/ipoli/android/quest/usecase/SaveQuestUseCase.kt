package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.Validator.Companion.validate
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.*
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.ReminderScheduler
import io.ipoli.android.quest.subquest.SubQuest
import io.ipoli.android.quest.usecase.Result.*
import io.ipoli.android.quest.usecase.Result.ValidationError.EMPTY_NAME
import io.ipoli.android.quest.usecase.Result.ValidationError.TIMER_RUNNING
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
        val category: Category,
        val startTime: Time? = null,
        val scheduledDate: LocalDate,
        val duration: Int,
        val reminder: Reminder? = null,
        val repeatingQuestId: String? = null,
        val note: String? = null
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
                category = parameters.category,
                scheduledDate = parameters.scheduledDate,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder,
                subQuests = subQuests,
                repeatingQuestId = parameters.repeatingQuestId,
                note = parameters.note
            )
        } else {

            val quest = questRepository.findById(parameters.id)!!

            if (quest.isStarted) {
                return Invalid(TIMER_RUNNING)
            }

            val subQuests = parameters.subQuests
                ?.filter { it.name.isNotBlank() } ?: quest.subQuests

            quest.copy(
                name = parameters.name.trim(),
                icon = parameters.icon,
                color = parameters.color,
                category = parameters.category,
                scheduledDate = parameters.scheduledDate,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder,
                subQuests = subQuests,
                repeatingQuestId = parameters.repeatingQuestId,
                note = parameters.note
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