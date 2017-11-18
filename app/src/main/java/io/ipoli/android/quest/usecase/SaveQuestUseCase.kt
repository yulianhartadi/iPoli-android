package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.Validator.Companion.validate
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.*
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.usecase.Result.*
import io.ipoli.android.quest.usecase.Result.ValidationError.EMPTY_NAME
import io.ipoli.android.reminder.ReminderScheduler
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 9/30/17.
 */
sealed class Result {

    enum class ValidationError {
        EMPTY_NAME
    }

    data class Added(val quest: Quest) : Result()
    data class Invalid(val errors: List<ValidationError>) : Result()
}

class SaveQuestUseCase(
    private val questRepository: QuestRepository,
    private val reminderScheduler: ReminderScheduler
) : UseCase<SaveQuestUseCase.Parameters, Result> {

    data class Parameters(val id: String = "",
                          val name: String,
                          val color: Color,
                          val icon: Icon? = null,
                          val category: Category,
                          val startTime: Time? = null,
                          val scheduledDate: LocalDate,
                          val duration: Int,
                          val reminder: Reminder? = null)

    override fun execute(parameters: Parameters): Result {
        val errors = validate(parameters).check<ValidationError> {
            "name" {
                given { name.isEmpty() } addError EMPTY_NAME
            }
        }

        if (errors.isNotEmpty()) {
            return Invalid(errors)
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
            questRepository.findById(parameters.id)!!.copy(
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

        val quests = questRepository.findNextQuestsToRemind(DateUtils.nowUTC().time)
        if (quests.isNotEmpty()) {
            reminderScheduler.schedule(quests.first().reminder!!.toMillis())
        }
        return Added(quest)
    }
}