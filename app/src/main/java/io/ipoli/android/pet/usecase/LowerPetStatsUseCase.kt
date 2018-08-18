package io.ipoli.android.pet.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.pet.Pet
import io.ipoli.android.player.data.Player
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.quest.Quest
import io.ipoli.android.quest.data.persistence.QuestRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
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

        val dateTime = parameters.dateTime.minusDays(1)

        val isPlanDay = player.preferences.planDays.contains(dateTime.toLocalDate().dayOfWeek)

        val highProductiveHours =
            if (isPlanDay) PLAN_DAY_HIGH_PRODUCTIVE_HOURS else NON_PLAN_DAY_HIGH_PRODUCTIVE_HOURS
        val mediumProductiveHours =
            if (isPlanDay) PLAN_DAY_MEDIUM_PRODUCTIVE_HOURS else NON_PLAN_DAY_MEDIUM_PRODUCTIVE_HOURS
        val lowProductiveHours =
            if (isPlanDay) PLAN_DAY_LOW_PRODUCTIVE_HOURS else NON_PLAN_DAY_LOW_PRODUCTIVE_HOURS

        val rhp = HEALTH_POINTS_PENALTIES[Random(randomSeed).nextInt(
            HEALTH_POINTS_PENALTIES.size
        )]

        val rmp = MOOD_POINTS_PENALTIES[Random(randomSeed).nextInt(
            MOOD_POINTS_PENALTIES.size
        )]

        val (start, end) = player.datesSpan(dateTime)

        val qs = end?.let {
            questRepository.findCompletedForDate(start) + questRepository.findCompletedForDate(it)
        } ?: questRepository.findCompletedForDate(start)

        val totalDuration = findQuestsDurationInInterval(
            start = start,
            end = end,
            resetDayTime = player.preferences.resetDayTime,
            quests = qs
        )

        if (totalDuration >= highProductiveHours * Time.MINUTES_IN_AN_HOUR) {
            return savePlayer(rhp, rmp)
        }

        if (totalDuration >= mediumProductiveHours * Time.MINUTES_IN_AN_HOUR) {
            return savePlayer(LOW_PENALTY + rhp, LOW_PENALTY + rmp)
        }

        if (totalDuration >= lowProductiveHours * Time.MINUTES_IN_AN_HOUR) {
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

        const val PLAN_DAY_HIGH_PRODUCTIVE_HOURS = 10
        const val PLAN_DAY_MEDIUM_PRODUCTIVE_HOURS = 6
        const val PLAN_DAY_LOW_PRODUCTIVE_HOURS = 3

        const val NON_PLAN_DAY_HIGH_PRODUCTIVE_HOURS = 5
        const val NON_PLAN_DAY_MEDIUM_PRODUCTIVE_HOURS = 3
        const val NON_PLAN_DAY_LOW_PRODUCTIVE_HOURS = 1

        fun findQuestsDurationInInterval(
            start: LocalDate,
            end: LocalDate?,
            resetDayTime: Time,
            quests: List<Quest>
        ) =
            quests.filter {
                when {
                    it.completedAtDate!! == start -> it.startTime != null && it.endTime!! >= resetDayTime
                    end != null && it.completedAtDate == end -> it.startTime != null && it.startTime < resetDayTime
                    else -> false
                }
            }.map {

                if (it.completedAtDate!! == start) {
                    val startMinute =
                        Math.max(resetDayTime.toMinuteOfDay(), it.startTime!!.toMinuteOfDay())
                    val endMinute = it.endTime!!.toMinuteOfDay()

                    if (endMinute < startMinute)
                        Time.MINUTES_IN_A_DAY - startMinute + endMinute
                    else
                        endMinute - startMinute
                } else {
                    val startMinute = it.startTime!!.toMinuteOfDay()
                    val endMinute =
                        Math.min(resetDayTime.toMinuteOfDay(), it.endTime!!.toMinuteOfDay())
                    endMinute - startMinute
                }

            }.fold(0) { acc, dur -> acc + dur }
    }

    data class Params(val dateTime: LocalDateTime = LocalDateTime.now())
}