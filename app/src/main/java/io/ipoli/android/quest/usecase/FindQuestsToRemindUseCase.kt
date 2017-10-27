package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/27/17.
 */
class FindQuestsToRemindUseCase(private val questRepository: QuestRepository) : UseCase<Long, List<Quest>> {
    override fun execute(parameters: Long) =
        questRepository.findQuestsToRemind(parameters)

}