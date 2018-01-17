package mypoli.android.quest.usecase

import mypoli.android.Constants
import mypoli.android.ReminderScheduler
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.datetime.Time
import mypoli.android.quest.data.persistence.QuestRepository
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
            val (scheduledDate, startTime) = calculateNewDateTime(it.scheduledDate, it.startTime!!)
            val (remindDate, remindTime) = calculateNewDateTime(
                it.reminder!!.remindDate,
                it.reminder.remindTime
            )

            it.copy(
                startTime = startTime,
                scheduledDate = scheduledDate,
                reminder = it.reminder.copy(remindTime = remindTime, remindDate = remindDate)
            )
        }
        questRepository.save(newQuest)
    }

    private fun scheduleNextReminder() {
        val quests = questRepository.findNextQuestsToRemind(DateUtils.nowUTC().time)
        require(quests.isNotEmpty())
        reminderScheduler.schedule(quests.first().reminder!!.toMillis())
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