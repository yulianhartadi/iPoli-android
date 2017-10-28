package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.reminder.ReminderScheduler

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/27/17.
 */
class UndoCompleteQuestUseCase(
    private val questRepository: QuestRepository,
    private val reminderScheduler: ReminderScheduler
) : UseCase<String, Unit> {
    override fun execute(parameters: String) {
        val newQuest = questRepository.findById(parameters)!!.copy(
            completedAtDate = null,
            completedAtTime = null
        )
        questRepository.save(newQuest)

        val quests = questRepository.findNextQuestsToRemind(DateUtils.nowUTC().time)
        if (quests.isNotEmpty()) {
            reminderScheduler.schedule(quests.first().reminder!!.toMillis())
        }
    }
}