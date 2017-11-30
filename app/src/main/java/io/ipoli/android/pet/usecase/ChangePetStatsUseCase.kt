package io.ipoli.android.pet.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.Pet
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 11/30/17.
 */
class ChangePetStatsUseCase(
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository
) : UseCase<Time, Pet> {

    override fun execute(parameters: Time): Pet {
        val time = parameters
        val morning = Time.atHours(9)
        val afternoon = Time.atHours(14)
        val evening = Time.atHours(19)

        val player = playerRepository.find()

        if(time.isBetween(evening, morning - 1)) {

        } else {
            val quests = questRepository.findCompletedForDate(LocalDate.now())
        }

        return player!!.pet
    }
}