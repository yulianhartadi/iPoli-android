package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository

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
            quests = questRepository.findNotRemovedForChallenge(challenge.id)
        )
    }


    data class Params(val challenge: Challenge)
}