package mypoli.android.pet.usecase

import mypoli.android.Constants
import mypoli.android.common.UseCase
import mypoli.android.common.datetime.Time
import mypoli.android.pet.Pet
import mypoli.android.player.persistence.PlayerRepository
import mypoli.android.quest.Quest
import mypoli.android.quest.data.persistence.QuestRepository
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
) : UseCase<Time, Pet> {

    override fun execute(parameters: Time): Pet {
        val player = playerRepository.find()
        requireNotNull(player)
        val pet = player!!.pet

        if (parameters == Constants.CHANGE_PET_STATS_MORNING_TIME) {

            val healthPenalty = MORNING_HEALTH_POINTS_PENALTIES[Random(randomSeed).nextInt(MORNING_HEALTH_POINTS_PENALTIES.size)]
            val moodPenalty = MORNING_MOOD_POINTS_PENALTIES[Random(randomSeed).nextInt(MORNING_MOOD_POINTS_PENALTIES.size)]

            return pet.removeHealthAndMoodPoints(healthPenalty, moodPenalty)
        }

        val (intervalStart, intervalEnd) = if (parameters == Constants.CHANGE_PET_STATS_AFTERNOON_TIME) {
            Pair(Constants.CHANGE_PET_STATS_MORNING_TIME, Constants.CHANGE_PET_STATS_AFTERNOON_TIME)
        } else {
            Pair(Constants.CHANGE_PET_STATS_AFTERNOON_TIME, Constants.CHANGE_PET_STATS_EVENING_TIME)
        }

        val totalDuration = findQuestsDurationInInterval(
            intervalStart,
            intervalEnd,
            questRepository.findCompletedForDate(LocalDate.now())
        )

        val intervalDuration = intervalStart.minutesTo(intervalEnd)

        if (totalDuration >= HIGH_PRODUCTIVE_TIME_COEF * intervalDuration) {
            return pet
        }

        if (totalDuration >= MEDIUM_PRODUCTIVE_TIME_COEF * intervalDuration) {
            return pet.removeHealthAndMoodPoints(LOW_PENALTY, LOW_PENALTY)
        }

        if (totalDuration >= LOW_PRODUCTIVE_TIME_COEF * intervalDuration) {
            return pet.removeHealthAndMoodPoints(MEDIUM_PENALTY, MEDIUM_PENALTY)
        }

        if (totalDuration > 0) {
            return pet.removeHealthAndMoodPoints(HIGH_PENALTY, HIGH_PENALTY)
        }

        return pet.removeHealthAndMoodPoints(MAX_PENALTY, MAX_PENALTY)
    }

    companion object {
        val MORNING_HEALTH_POINTS_PENALTIES = intArrayOf(3, 4, 5, 6, 7)
        val MORNING_MOOD_POINTS_PENALTIES = intArrayOf(3, 4, 5, 6, 7)

        val MAX_PENALTY = 15
        val HIGH_PENALTY = 10
        val MEDIUM_PENALTY = 5
        val LOW_PENALTY = 2

        val LOW_PRODUCTIVE_TIME_COEF = 0.3
        val MEDIUM_PRODUCTIVE_TIME_COEF = 0.5
        val HIGH_PRODUCTIVE_TIME_COEF = 0.7

        /**
         * @param start inclusive
         * @param end inclusive
         */
        fun findQuestsDurationInInterval(start: Time, end: Time, quests: List<Quest>) =
            quests.filter {
                val qStart = it.startTime!! + 1
                val qEnd = it.endTime!! - 1
                qEnd >= start && qStart <= end
            }.map {
                val startMinute = Math.max(start.toMinuteOfDay(), it.startTime!!.toMinuteOfDay())
                val endMinute = Math.min(end.toMinuteOfDay(), it.endTime!!.toMinuteOfDay())
                endMinute - startMinute
            }.fold(0, { acc, dur -> acc + dur })
    }
}