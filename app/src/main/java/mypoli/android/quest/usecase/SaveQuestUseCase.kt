package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.Validator.Companion.validate
import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.quest.job.ReminderScheduler
import mypoli.android.quest.usecase.Result.*
import mypoli.android.quest.usecase.Result.ValidationError.EMPTY_NAME
import mypoli.android.quest.usecase.Result.ValidationError.TIMER_RUNNING
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
        val color: Color,
        val icon: Icon? = null,
        val category: Category,
        val startTime: Time? = null,
        val scheduledDate: LocalDate,
        val duration: Int,
        val reminder: Reminder? = null
    )

    override fun execute(parameters: Parameters): Result {
        val errors = validate(parameters).check<ValidationError> {
            "name" {
                given { name.isEmpty() } addError EMPTY_NAME
            }
        }

        if (errors.isNotEmpty()) {
            return Invalid(errors.first())
        }
        val quest = if (parameters.id.isEmpty()) {
            Quest(
                name = parameters.name,
                icon = parameters.icon,
                color = parameters.color,
                category = parameters.category,
                scheduledDate = parameters.scheduledDate,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder
            )
        } else {

            val quest = questRepository.findById(parameters.id)!!

            if (quest.isStarted) {
                return Invalid(TIMER_RUNNING)
            }

            quest.copy(
                name = parameters.name,
                icon = parameters.icon,
                color = parameters.color,
                category = parameters.category,
                scheduledDate = parameters.scheduledDate,
                startTime = parameters.startTime,
                duration = parameters.duration,
                reminder = parameters.reminder
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