package io.ipoli.android.challenge.usecase

import io.ipoli.android.challenge.entity.Challenge
import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.BaseQuest
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.tag.Tag
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/09/2018.
 */
class SaveChallengeUseCase(
    private val challengeRepository: ChallengeRepository,
    private val saveQuestsForChallengeUseCase: SaveQuestsForChallengeUseCase
) :
    UseCase<SaveChallengeUseCase.Params, Challenge> {

    override fun execute(parameters: Params): Challenge {

        require(parameters.name.isNotEmpty())

        return if (parameters.id.isNotEmpty()) {
            updateChallenge(parameters)
        } else {
            saveNewChallenge(parameters)
        }
    }

    private fun updateChallenge(parameters: Params): Challenge {
        val c = challengeRepository.findById(parameters.id)
        require(c != null)

        return challengeRepository.save(
            c!!.copy(
                name = parameters.name.trim(),
                tags = parameters.tags,
                color = parameters.color,
                icon = parameters.icon,
                difficulty = parameters.difficulty,
                endDate = parameters.end,
                motivations = transformMotivations(parameters.motivations),
                note = parameters.note
            )
        )
    }

    private fun saveNewChallenge(parameters: Params): Challenge {
        val c = challengeRepository.save(
            Challenge(
                name = parameters.name.trim(),
                tags = parameters.tags,
                color = parameters.color,
                icon = parameters.icon,
                difficulty = parameters.difficulty,
                startDate = LocalDate.now(),
                endDate = parameters.end,
                motivations = transformMotivations(parameters.motivations),
                note = parameters.note
            )
        )

        saveQuestsForChallengeUseCase.execute(
            when (parameters) {
                is Params.WithNewQuests ->
                    SaveQuestsForChallengeUseCase.Params.WithNewQuests(
                        c.id,
                        parameters.quests
                    )
                is Params.WithExistingQuests ->
                    SaveQuestsForChallengeUseCase.Params.WithExistingQuests(
                        c.id,
                        parameters.allQuests,
                        parameters.selectedQuestIds
                    )
            }

        )

        return c
    }

    sealed class Params(
        open val id: String = "",
        open val name: String,
        open val tags: List<Tag>,
        open val color: Color,
        open val icon: Icon?,
        open val difficulty: Challenge.Difficulty,
        open val motivations: List<String>,
        open val end: LocalDate,
        open val note: String
    ) {
        data class WithNewQuests(
            val quests: List<BaseQuest>,
            override val id: String = "",
            override val name: String,
            override val tags: List<Tag>,
            override val color: Color,
            override val icon: Icon?,
            override val difficulty: Challenge.Difficulty,
            override val motivations: List<String>,
            override val end: LocalDate,
            override val note: String = ""

        ) : Params(id, name, tags, color, icon, difficulty, motivations, end, note)

        data class WithExistingQuests(
            val allQuests: List<BaseQuest> = listOf(),
            val selectedQuestIds: Set<String> = setOf(),
            override val id: String = "",
            override val name: String,
            override val tags: List<Tag>,
            override val color: Color,
            override val icon: Icon?,
            override val difficulty: Challenge.Difficulty,
            override val motivations: List<String>,
            override val end: LocalDate,
            override val note: String = ""

        ) : Params(id, name, tags, color, icon, difficulty, motivations, end, note)
    }

    private fun transformMotivations(motivations: List<String>) =
        motivations.filter { it.isNotBlank() }.map { it.trim() }
}