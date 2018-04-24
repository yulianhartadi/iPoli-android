package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDateTime

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 10/27/17.
 */
class FindQuestsToRemindUseCase(private val questRepository: QuestRepository) :
    UseCase<LocalDateTime, List<Quest>> {

    override fun execute(parameters: LocalDateTime) =
        questRepository.findQuestsToRemind(parameters)
}