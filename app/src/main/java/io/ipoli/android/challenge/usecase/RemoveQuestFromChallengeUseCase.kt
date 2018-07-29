package io.ipoli.android.challenge.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/09/2018.
 */
class RemoveQuestFromChallengeUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository
) :
    UseCase<RemoveQuestFromChallengeUseCase.Params, RemoveQuestFromChallengeUseCase.Result> {

    override fun execute(parameters: Params) =
        when (parameters) {
            is Params.WithQuestId -> {
                require(parameters.id.isNotEmpty())
                val quest = questRepository.findById(parameters.id)
                require(quest != null)
                Result.ChangedQuest(questRepository.removeFromChallenge(quest!!))
            }

            is Params.WithRepeatingQuestId -> {
                require(parameters.id.isNotEmpty())
                val rq = repeatingQuestRepository.findById(parameters.id)
                require(rq != null)

                questRepository.removeFromChallenge(
                    questRepository
                        .findAllForRepeatingQuest(rq!!.id)
                )

                Result.ChangedRepeatingQuest(
                    repeatingQuestRepository.removeFromChallenge(rq)
                )
            }
        }


    sealed class Params {
        data class WithQuestId(val id: String) : Params()
        data class WithRepeatingQuestId(val id: String) : Params()
    }

    sealed class Result {
        data class ChangedQuest(val quest: Quest) : Result()
        data class ChangedRepeatingQuest(val quest: RepeatingQuest) : Result()
    }


}