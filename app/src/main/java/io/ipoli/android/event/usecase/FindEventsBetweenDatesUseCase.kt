package io.ipoli.android.event.usecase

import io.ipoli.android.common.UseCase
import io.ipoli.android.common.permission.PermissionChecker
import io.ipoli.android.event.Event
import io.ipoli.android.event.persistence.EventRepository
import io.ipoli.android.player.persistence.PlayerRepository
import io.ipoli.android.store.powerup.PowerUp
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 04/02/2018.
 */
class FindEventsBetweenDatesUseCase(
    private val playerRepository: PlayerRepository,
    private val eventRepository: EventRepository,
    private val permissionChecker: PermissionChecker
) : UseCase<FindEventsBetweenDatesUseCase.Params, List<Event>> {

    override fun execute(parameters: Params): List<Event> {

        if (!permissionChecker.canReadCalendar()) {
            return emptyList()
        }

        val p = playerRepository.find() ?: return emptyList()

        if (!p.isPowerUpEnabled(PowerUp.Type.CALENDAR_SYNC)) {
            return emptyList()
        }

        val calendars = p.preferences.syncCalendars

        if (calendars.isEmpty()) {
            return emptyList()
        }

        return eventRepository.findScheduledBetween(
            calendars.map { it.id.toInt() }.toSet(),
            parameters.startDate,
            parameters.endDate
        )
    }

    data class Params(val startDate: LocalDate, val endDate: LocalDate)
}