package io.ipoli.android.tag.usecase

import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.replace
import io.ipoli.android.quest.Color
import io.ipoli.android.quest.Icon
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.persistence.TagRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/04/2018.
 */
class SaveTagUseCase(
    private val tagRepository: TagRepository,
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository,
    private val challengeRepository: ChallengeRepository
) :
    UseCase<SaveTagUseCase.Params, Tag> {

    override fun execute(parameters: Params): Tag {
        require(parameters.name.isNotBlank())

        val tag = tagRepository.save(
            Tag(
                id = parameters.id ?: "",
                name = parameters.name,
                color = parameters.color,
                icon = parameters.icon,
                isFavorite = parameters.isFavorite
            )
        )

        val isUpdating = parameters.id != null

        if (isUpdating) {
            updateQuests(tag)
            updateRepeatingQuests(tag)
            updateChallenges(tag)
        }

        return tag
    }

    private fun updateQuests(tag: Tag) {
        val tQuests = questRepository.findByTag(tag.id)
        val qs = tQuests.map {
            it.copy(
                tags = it.tags.replace({ it.id == tag.id }, { tag })
            )
        }
        questRepository.save(qs)
    }

    private fun updateRepeatingQuests(tag: Tag) {
        val rqs = repeatingQuestRepository.findByTag(tag.id)
        val uRqs = rqs.map {
            it.copy(
                tags = it.tags.replace({ it.id == tag.id }, { tag })
            )
        }
        repeatingQuestRepository.save(uRqs)
    }

    private fun updateChallenges(tag: Tag) {
        val cs = challengeRepository.findByTag(tag.id)
        val uCs = cs.map {
            it.copy(
                tags = it.tags.replace({ it.id == tag.id }, { tag })
            )
        }
        challengeRepository.save(uCs)
    }

    data class Params(
        val id: String?,
        val name: String,
        val icon: Icon?,
        val color: Color,
        val isFavorite: Boolean
    )
}