package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.reminder.ReminderScheduler
import org.threeten.bp.LocalDate

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/9/17.
 */
class RemoveQuestUseCase(private val questRepository: QuestRepository, private val reminderScheduler: ReminderScheduler) : UseCase<String, Unit> {
    override fun execute(parameters: String) {
        if (parameters.isEmpty()) {
            return
        }
        questRepository.remove(parameters)
        val quests = questRepository.findNextQuestsToRemind(DateUtils.toMillis(LocalDate.now()))
        if (quests.isNotEmpty()) {
            val reminder = quests[0].reminder!!
            reminderScheduler.schedule(reminder.toMillis())
        }
    }
}