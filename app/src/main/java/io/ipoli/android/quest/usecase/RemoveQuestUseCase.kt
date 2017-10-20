package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 10/9/17.
 */
class RemoveQuestUseCase(private val questRepository: QuestRepository) : UseCase<String, Unit> {
    override fun execute(parameters: String) {
        questRepository.remove(parameters)
    }
}