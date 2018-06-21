package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.habit.persistence.HabitRepository
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.quest.job.ReminderScheduler
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/12/2018.
 */
class RemoveChallengeUseCase(
    private val challengeRepository: ChallengeRepository,
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository,
    private val habitRepository: HabitRepository,
    private val reminderScheduler: ReminderScheduler
) : UseCase<RemoveChallengeUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val c = challengeRepository.findById(parameters.challengeId)
        require(c != null)

        val habits = habitRepository.findAllForChallenge(c!!.id)
        habitRepository.save(habits.map { it.copy(challengeId = null) })

        val questsToUpdate = mutableListOf<Quest>()
        val questsToPurge = mutableListOf<Quest>()

        questRepository
            .findAllForChallenge(c.id)
            .forEach {
                if (it.isCompleted) {
                    questsToUpdate.add(
                        it.copy(
                            repeatingQuestId = null,
                            challengeId = null
                        )
                    )
                } else {
                    questsToPurge.add(it)
                }
            }

        questRepository.save(questsToUpdate)
        questRepository.purge(questsToPurge.map { it.id })

        val rqs = repeatingQuestRepository
            .findAllForChallenge(c.id)
        repeatingQuestRepository.purge(rqs.map { it.id })
        reminderScheduler.schedule()
        challengeRepository.remove(c)
    }

    data class Params(val challengeId: String)
}