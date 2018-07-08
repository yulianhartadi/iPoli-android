package io.ipoli.android.event

import io.ipoli.android.common.datetime.*
import io.ipoli.android.quest.Entity
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 03/09/2018.
 */
data class Event(
    override val id: String = "",
    val name: String,
    val startTime: Time,
    val endTime: Time,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val color: Int,
    val isRepeating: Boolean,
    val isAllDay: Boolean,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity {
    val duration: Duration<Minute>
        get() = ((endTime - startTime).toMinuteOfDay() +
            startDate.plusDays(1).daysBetween(endDate) * Time.MINUTES_IN_A_DAY).minutes

}