package io.ipoli.android.tag.usecase

import io.ipoli.android.challenge.persistence.ChallengeRepository
import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository
import io.ipoli.android.repeatingquest.persistence.RepeatingQuestRepository
import io.ipoli.android.tag.Tag
import io.ipoli.android.tag.persistence.TagRepository

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/07/2018.
 */
class RemoveTagUseCase(
    private val questRepository: QuestRepository,
    private val repeatingQuestRepository: RepeatingQuestRepository,
    private val challengeRepository: ChallengeRepository,
    private val tagRepository: TagRepository
) :
    UseCase<RemoveTagUseCase.Params, Unit> {

    override fun execute(parameters: Params) {
        val newQuests = questRepository
            .findByTag(parameters.tagId).map {
                it.copy(
                    tags = removeTag(parameters.tagId, it.tags)
                )
            }
        questRepository.save(newQuests)

        val newRepeatingQuests = repeatingQuestRepository
            .findByTag(parameters.tagId)
            .map {
                it.copy(
                    tags = removeTag(parameters.tagId, it.tags)
                )
            }

        repeatingQuestRepository.save(newRepeatingQuests)

        val newChallenges = challengeRepository
            .findByTag(parameters.tagId)
            .map {
                it.copy(
                    tags = removeTag(parameters.tagId, it.tags)
                )
            }
        challengeRepository.save(newChallenges)

        tagRepository.purge(parameters.tagId)
    }

    private fun removeTag(tagId: String, tags: List<Tag>) =
        tags.filter { it.id != tagId }

    data class Params(val tagId: String)
}