package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.quest.QuestSchedule
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/27/17.
 */
class CompleteQuestUseCase(private val questRepository: QuestRepository) : UseCase<String, Unit> {
    override fun execute(parameters: String) {
        val newQuest = questRepository.findById(parameters)!!.let {
            it.copy(
                completedAtDate = LocalDate.now(),
                actualSchedule = QuestSchedule(
                    date = LocalDate.now(),
                    time = Time.now(),
                    duration = it.plannedSchedule.duration
                )
            )
        }
        questRepository.save(newQuest)
    }
}