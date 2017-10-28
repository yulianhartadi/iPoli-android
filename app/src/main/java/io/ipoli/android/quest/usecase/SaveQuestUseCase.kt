package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.Validator.Companion.validate
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.usecase.Result.*
import io.ipoli.android.quest.usecase.Result.ValidationError.EMPTY_NAME
import io.ipoli.android.reminder.ReminderScheduler

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
) : UseCase<Quest, Result> {
    override fun execute(parameters: Quest): Result {
        val quest = parameters
        val errors = validate(quest).check<ValidationError> {
            "name" {
                given { name.isEmpty() } addError EMPTY_NAME
            }
        }

        if (errors.isNotEmpty()) {
            return Invalid(errors)
        }
        questRepository.save(quest)

        val quests = questRepository.findNextQuestsToRemind(DateUtils.nowUTC().time)
        if (quests.isNotEmpty()) {
            reminderScheduler.schedule(quests.first().reminder!!.toMillis())
        }
        return Added(quest)
    }
}