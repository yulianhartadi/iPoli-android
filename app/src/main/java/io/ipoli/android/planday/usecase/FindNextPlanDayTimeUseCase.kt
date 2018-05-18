package io.ipoli.android.planday.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.datetime.Time
import io.ipoli.android.player.persistence.PlayerRepository
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 05/18/2018.
 */
class FindNextPlanDayTimeUseCase(private val playerRepository: PlayerRepository) :
    UseCase<FindNextPlanDayTimeUseCase.Params, LocalDateTime?> {

    fun execute(): LocalDateTime? = execute(Params())

    override fun execute(parameters: Params): LocalDateTime? {
        val player = playerRepository.find()
        requireNotNull(player)

        val prefs = player!!.preferences

        val days = prefs.planDays

        if (days.isEmpty()) {
            return null
        }

        val time = prefs.planDayTime

        var currentDate = parameters.currentDate
        val currentTime = parameters.currentTime

        if (days.contains(currentDate.dayOfWeek) && currentTime <= time) {
            return LocalDateTime.of(
                currentDate,
                LocalTime.of(time.hours, time.getMinutes())
            )
        }

        while (true) {

            currentDate = currentDate.plusDays(1)

            if (days.contains(currentDate.dayOfWeek)) {
                return LocalDateTime.of(
                    currentDate,
                    LocalTime.of(time.hours, time.getMinutes())
                )
            }
        }
    }

    data class Params(
        val currentDate: LocalDate = LocalDate.now(),
        val currentTime: Time = Time.now()
    )

}