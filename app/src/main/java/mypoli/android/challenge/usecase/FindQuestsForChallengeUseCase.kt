package mypoli.android.challenge.usecase

import mypoli.android.challenge.entity.Challenge
import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/13/18.
 */
class FindQuestsForChallengeUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository
) : UseCase<FindQuestsForChallengeUseCase.Params, Challenge> {

    override fun execute(parameters: Params): Challenge {
        val challenge = parameters.challenge
        val rqs = repeatingQuestRepository.findAllForChallenge(challenge.id)
        val quests = questRepository.findAllForChallengeNotRepeating(challenge.id)
        return challenge.copy(
            baseQuests = (rqs + quests),
            repeatingQuests = rqs,
            quests = questRepository.findAllForChallenge(challenge.id)
        )
    }


    data class Params(val challenge: Challenge)
}