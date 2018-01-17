package mypoli.android.quest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/27/17.
 */
class FindQuestsToRemindUseCase(private val questRepository: QuestRepository) :
    UseCase<Long, List<Quest>> {
    override fun execute(parameters: Long) =
        questRepository.findQuestsToRemind(parameters)

}