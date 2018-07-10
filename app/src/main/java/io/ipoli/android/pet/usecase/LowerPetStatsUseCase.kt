package io.ipoli.android.pet.usecase

import io.ipoli.android.Constants
import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.Pet
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate
import java.util.*

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 11/30/17.
 */
class LowerPetStatsUseCase(
    private val questRepository: QuestRepository,
    private val playerRepository: PlayerRepository,
    private val randomSeed: Long = System.currentTimeMillis()
) : UseCase<LowerPetStatsUseCase.Params, Pet> {

    private lateinit var player: Player

    override fun execute(parameters: Params): Pet {
        val p = playerRepository.find()
        requireNotNull(p)
        player = p!!

        if (player.pet.isDead) {
            return player.pet
        }

        val rhp = HEALTH_POINTS_PENALTIES[Random(randomSeed).nextInt(
            HEALTH_POINTS_PENALTIES.size
            )]

        val rmp = MOOD_POINTS_PENALTIES[Random(randomSeed).nextInt(
                MOOD_POINTS_PENALTIES.size
            )]

        val intervalStart = Constants.CHANGE_PET_STATS_INTERVAL_START
        val intervalEnd = Constants.CHANGE_PET_STATS_INTERVAL_END
        val totalDuration = findQuestsDurationInInterval(
            intervalStart,
            intervalEnd,
            questRepository.findCompletedForDate(parameters.date)
        )

        val intervalDuration = intervalStart.minutesTo(intervalEnd)

        if (totalDuration >= HIGH_PRODUCTIVE_TIME_COEF * intervalDuration) {
            return savePlayer(rhp, rmp)
        }

        if (totalDuration >= MEDIUM_PRODUCTIVE_TIME_COEF * intervalDuration) {
            return savePlayer(LOW_PENALTY + rhp, LOW_PENALTY + rmp)
        }

        if (totalDuration >= LOW_PRODUCTIVE_TIME_COEF * intervalDuration) {
            return savePlayer(MEDIUM_PENALTY + rhp, MEDIUM_PENALTY + rmp)
        }

        if (totalDuration > 0) {
            return savePlayer(HIGH_PENALTY + rhp, HIGH_PENALTY + rmp)
        }

        return savePlayer(MAX_PENALTY + rhp, MAX_PENALTY + rmp)
    }

    private fun savePlayer(healthPenalty: Int, moodPenalty: Int) =
        playerRepository.save(
            player.copy(
                pet = player.pet.removeHealthAndMoodPoints(healthPenalty, moodPenalty)
            )
        ).pet


    companion object {
        val HEALTH_POINTS_PENALTIES = intArrayOf(3, 4, 5, 6, 7)
        val MOOD_POINTS_PENALTIES = intArrayOf(3, 4, 5, 6, 7)

        const val MAX_PENALTY = 25
        const val HIGH_PENALTY = 18
        const val MEDIUM_PENALTY = 8
        const val LOW_PENALTY = 4

        const val LOW_PRODUCTIVE_TIME_COEF = 0.3
        const val MEDIUM_PRODUCTIVE_TIME_COEF = 0.5
        const val HIGH_PRODUCTIVE_TIME_COEF = 0.7

        /**
         * @param start inclusive
         * @param end inclusive
         */
        fun findQuestsDurationInInterval(start: Time, end: Time, quests: List<Quest>) =
            quests.filter {
                it.startTime != null && it.endTime!! - 1 >= start && it.startTime + 1 <= end
            }.map {
                val startMinute =
                    Math.max(start.toMinuteOfDay(), it.startTime!!.toMinuteOfDay())
                val endMinute = Math.min(end.toMinuteOfDay(), it.endTime!!.toMinuteOfDay())
                endMinute - startMinute
            }.fold(0) { acc, dur -> acc + dur }
    }

    data class Params(val date: LocalDate = LocalDate.now())
}