package io.ipoli.android.quest.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/27/17.
 */
class CompleteQuestUseCase(private val questRepository: QuestRepository) : UseCase<String, Unit> {
    override fun execute(parameters: String) {

    }
}