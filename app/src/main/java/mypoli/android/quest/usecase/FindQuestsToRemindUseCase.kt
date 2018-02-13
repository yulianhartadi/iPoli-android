package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/27/17.
 */
class FindQuestsToRemindUseCase(private val questRepository: QuestRepository) :
    UseCase<LocalDateTime, List<Quest>> {
    override fun execute(parameters: LocalDateTime): List<Quest> {
        return questRepository.findQuestsToRemind(parameters)
    }

}