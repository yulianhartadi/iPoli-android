package io.ipoli.android.quest.usecase

import io.ipoli.android.Constants
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.ReminderScheduler
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/27/17.
 */
class SnoozeQuestUseCase(
    private val questRepository: QuestRepository,
    private val reminderScheduler: ReminderScheduler
) : UseCase<String, Unit> {
    override fun execute(parameters: String) {
        updateQuest(parameters)
        scheduleNextReminder()
    }

    private fun updateQuest(parameters: String) {
        val newQuest = questRepository.findById(parameters)!!.let {
            val (scheduledDate, startTime) = calculateNewDateTime(
                it.scheduledDate!!,
                it.startTime!!
            )

            it.copy(
                startTime = startTime,
                scheduledDate = scheduledDate
            )
        }
        questRepository.save(newQuest)
    }

    private fun scheduleNextReminder() {
        reminderScheduler.schedule()
    }

    private fun calculateNewDateTime(date: LocalDate, time: Time): Pair<LocalDate, Time> {
        val newTime = time.toMinuteOfDay() + Constants.DEFAULT_SNOOZE_TIME_MINUTES
        if (newTime > Time.MINUTES_IN_A_DAY) {
            return Pair(
                date.plusDays(1),
                Time.of(newTime - Time.MINUTES_IN_A_DAY)
            )
        }

        return Pair(date, Time.of(newTime))
    }

}