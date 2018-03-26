package io.ipoli.android.repeatingquest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/2/18.
 */
class RemoveRepeatingQuestUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository
) : UseCase<RemoveRepeatingQuestUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        questRepository.purgeAllNotCompletedForRepeating(parameters.repeatingQuestId)
        repeatingQuestRepository.remove(parameters.repeatingQuestId)
    }

    data class Params(val repeatingQuestId: String)
}