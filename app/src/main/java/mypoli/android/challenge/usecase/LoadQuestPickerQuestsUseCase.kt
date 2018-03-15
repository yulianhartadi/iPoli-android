package mypoli.android.challenge.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.Quest
import mypoli.android.quest.RepeatingQuest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/12/18.
 */
class LoadQuestPickerQuestsUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository
) : UseCase<LoadQuestPickerQuestsUseCase.Params, LoadQuestPickerQuestsUseCase.Result> {

    override fun execute(parameters: Params): Result {
        val challengeId = parameters.challengeId
        return Result(
            quests = questRepository.findNotCompletedNotForChallengeNotRepeating(challengeId),
            repeatingQuests = repeatingQuestRepository.findActiveNotForChallenge(challengeId)
        )
    }

    data class Params(val challengeId: String)
    data class Result(val quests: List<Quest>, val repeatingQuests: List<RepeatingQuest>)
}