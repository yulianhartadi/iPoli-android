package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.common.Validator.Companion.validate
import mypoli.android.common.datetime.Time
import mypoli.android.quest.Category
import mypoli.android.quest.Color
import mypoli.android.quest.Icon
import mypoli.android.quest.Reminder
import mypoli.android.repeatingquest.entity.RepeatingPattern
import mypoli.android.repeatingquest.entity.RepeatingQuest
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 2/23/18.
 */
class SaveRepeatingQuestUseCase(
    private val repeatingQuestRepository: RepeatingQuestRepository
) : UseCase<SaveRepeatingQuestUseCase.Params, SaveRepeatingQuestUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val errors = validate(parameters).check<Result.ValidationError> {
            "name" {
                given { name.isEmpty() } addError Result.ValidationError.EMPTY_NAME
            }
        }

        if (errors.isNotEmpty()) {
            return Result.Invalid(errors.first())
        }
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

        repeatingQuestRepository.save(repeatingQuest)

        return Result.Added(repeatingQuest)
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

    sealed class Result {

        enum class ValidationError {
            EMPTY_NAME
        }

        data class Added(val repeatingQuest: RepeatingQuest) : Result()
        data class Invalid(val error: ValidationError) : Result()
    }
}