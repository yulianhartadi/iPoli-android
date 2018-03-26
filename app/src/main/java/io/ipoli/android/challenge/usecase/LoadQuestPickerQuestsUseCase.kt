package io.ipoli.android.challenge.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository

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