package mypoli.android.challenge.usecase

import mypoli.android.common.UseCase
import mypoli.android.quest.BaseQuest
import mypoli.android.quest.Quest
import mypoli.android.quest.RepeatingQuest
import mypoli.android.quest.data.persistence.QuestRepository
import mypoli.android.repeatingquest.persistence.RepeatingQuestRepository
import mypoli.android.repeatingquest.usecase.SaveRepeatingQuestUseCase

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 3/8/18.
 */
open class SaveQuestsForChallengeUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository,
    private val saveRepeatingQuestUseCase: SaveRepeatingQuestUseCase
) : UseCase<SaveQuestsForChallengeUseCase.Params, Unit> {

    override fun execute(parameters: SaveQuestsForChallengeUseCase.Params) {

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

        quests.forEach {
            questRepository.save((it as Quest).copy(challengeId = challengeId))
        }

        when (parameters) {
            is Params.WithNewQuests -> {
                repeatingQuests.forEach {
                    val rq = it as RepeatingQuest
                    saveRepeatingQuestUseCase.execute(
                        SaveRepeatingQuestUseCase.Params(
                            name = rq.name,
                            color = rq.color,
                            icon = rq.icon,
                            category = rq.category,
                            startTime = rq.startTime,
                            duration = rq.duration,
                            reminder = rq.reminder,
                            challengeId = challengeId,
                            repeatingPattern = rq.repeatingPattern
                        )
                    )
                }
            }
            is Params.WithExistingQuests -> {
                repeatingQuests.forEach {
                    repeatingQuestRepository.save((it as RepeatingQuest).copy(challengeId = challengeId))
                    questRepository.findAllForRepeatingQuestAfterDate(it.id, true).forEach {
                        questRepository.save(it.copy(challengeId = challengeId))
                    }
                }
            }
        }
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