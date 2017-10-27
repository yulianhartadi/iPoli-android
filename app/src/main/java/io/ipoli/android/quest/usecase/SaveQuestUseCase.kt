package io.ipoli.android.quest.usecase

import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import io.ipoli.android.ReminderNotificationJob
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.Validator.Companion.validate
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.toMillis
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.usecase.Result.*
import io.ipoli.android.quest.usecase.Result.ValidationError.EMPTY_NAME
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

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

class SaveQuestUseCase(private val questRepository: QuestRepository) : UseCase<Quest, Result> {
    override fun execute(parameters: Quest): Result {
        val quest = parameters
        val errors = validate(quest).check<ValidationError> {
            "name" {
                given { name.isEmpty() } addError EMPTY_NAME
            }
        }

        if (errors.isEmpty()) {
            questRepository.save(quest)
            val quests = questRepository.findNextQuestsToRemind(DateUtils.toMillis(LocalDate.now()))
            if (quests.isNotEmpty()) {
                val reminder = quests[0].reminder!!
                val date = reminder.remindDate
                val time = reminder.remindTime
                val dateTime = LocalDateTime.of(date, LocalTime.of(time.hours, time.getMinutes()))

                val bundle = PersistableBundleCompat()
                bundle.putLong("start", dateTime.toMillis())

                JobRequest.Builder(ReminderNotificationJob.TAG)
                    .setExtras(bundle)
//                    .setExact(dateTime.toMillis() - System.currentTimeMillis())
                    .setExact(100)
                    .build()
                    .schedule()
            }
            return Added(quest)
        }
        return Invalid(errors)
    }
}