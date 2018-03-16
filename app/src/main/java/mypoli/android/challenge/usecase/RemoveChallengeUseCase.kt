package mypoli.android.challenge.usecase

import mypoli.android.challenge.persistence.ChallengeRepository
import mypoli.android.common.UseCase
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/12/2018.
 */
class RemoveChallengeUseCase(
    private val challengeRepository: ChallengeRepository,
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository
) : UseCase<RemoveChallengeUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val c = challengeRepository.findById(parameters.challengeId)
        require(c != null)

        val questsToPurge = mutableListOf<String>()

        questRepository
            .findAllForChallenge(c!!.id)
            .forEach {
                if (it.isCompleted) {
                    questRepository.save(
                        it.copy(
                            repeatingQuestId = null,
                            challengeId = null
                        )
                    )
                } else {
                    questsToPurge.add(it.id)
                }
            }

        questRepository.purge(questsToPurge)

        val rqs = repeatingQuestRepository
            .findAllForChallenge(c.id)
        repeatingQuestRepository.purge(rqs.map { it.id })
        challengeRepository.remove(c)
    }

    data class Params(val challengeId: String)
}