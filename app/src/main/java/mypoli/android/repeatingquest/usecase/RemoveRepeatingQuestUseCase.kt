package mypoli.android.repeatingquest.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
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

    data class Params(val repeatingQuestId : String)
}