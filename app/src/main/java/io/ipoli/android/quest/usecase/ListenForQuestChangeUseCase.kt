package io.ipoli.android.quest.usecase

import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.map
import io.ipoli.android.common.StreamingUseCase
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 01/16/2018.
 */
class ListenForQuestChangeUseCase(
    private val questRepository: QuestRepository
) : StreamingUseCase<ListenForQuestChangeUseCase.Params, Quest> {

    override fun execute(parameters: Params): ReceiveChannel<Quest> {
        require(parameters.questId.isNotEmpty())
        return questRepository.listenById(parameters.questId).map {
            it!!
        }
    }

    data class Params(val questId: String)
}