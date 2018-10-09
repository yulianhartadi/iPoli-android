package io.ipoli.android.challenge.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.BaseQuest
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.RepeatingQuest
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository
import io.ipoli.android.repeatingquest.usecase.SaveRepeatingQuestUseCase

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/8/18.
 */
open class SaveQuestsForChallengeUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository,
    private val saveRepeatingQuestUseCase: SaveRepeatingQuestUseCase
) : UseCase<SaveQuestsForChallengeUseCase.Params, List<BaseQuest>> {

    override fun execute(parameters: SaveQuestsForChallengeUseCase.Params): List<BaseQuest> {

        val result = mutableListOf<BaseQuest>()

        val challengeId = parameters.challengeId

        val (quests, repeatingQuests) = when (parameters) {
            is Params.WithNewQuests -> {
                parameters.quests
                    .partition {
                        it is Quest
                    }
            }

            is Params.WithExistingQuests -> {
                val allQuests = parameters.allQuests

                allQuests
                    .filter {
                        parameters.selectedQuestIds.contains(
                            it.id
                        )
                    }.partition {
                        it is Quest
                    }
            }
        }

        quests
            .map { (it as Quest).copy(challengeId = challengeId) }
            .let { result.addAll(questRepository.save(it)) }

        when (parameters) {
            is Params.WithNewQuests -> {
                repeatingQuests.forEach {
                    val rq = it as RepeatingQuest
                    result.add(
                        saveRepeatingQuestUseCase.execute(
                            SaveRepeatingQuestUseCase.Params(
                                name = rq.name,
                                subQuestNames = rq.subQuests.map { sq -> sq.name },
                                color = rq.color,
                                icon = rq.icon,
                                startTime = rq.startTime,
                                duration = rq.duration,
                                reminders = rq.reminders,
                                challengeId = challengeId,
                                repeatPattern = rq.repeatPattern
                            )
                        )
                    )
                }
            }
            is Params.WithExistingQuests -> {

                repeatingQuests
                    .map { (it as RepeatingQuest).copy(challengeId = challengeId) }
                    .let { result.addAll(repeatingQuestRepository.save(it)) }

                val rqIds = repeatingQuests.map { it.id }

                rqIds
                    .map { questRepository.findAllForRepeatingQuestAfterDate(it, true) }
                    .flatten()
                    .map { it.copy(challengeId = challengeId) }
                    .let { result.addAll(questRepository.save(it)) }
            }
        }

        return result
    }

    sealed class Params(
        open val challengeId: String
    ) {
        data class WithNewQuests(
            override val challengeId: String,
            val quests: List<BaseQuest>
        ) : Params(challengeId)

        data class WithExistingQuests(
            override val challengeId: String,
            val allQuests: List<BaseQuest>,
            val selectedQuestIds: Set<String>
        ) : Params(challengeId)
    }

}