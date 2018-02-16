package mypoli.android.repeatingquest.entity

import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 02/14/2018.
 */

sealed class RepeatingPattern {
    object Daily : RepeatingPattern()
    data class Yearly(val dayOfMonth: Int, val month: Int) : RepeatingPattern()
    data class Weekly(val daysOfWeek: Set<DayOfWeek>) :
        RepeatingPattern()

    data class Monthly(val daysOfMonth: Set<Int>) :
        RepeatingPattern()

    sealed class Flexible : RepeatingPattern() {
        data class Weekly(
            val timesPerWeek: Int,
            val preferredDays: Set<DayOfWeek>,
            val scheduledPeriods : Map<LocalDate, List<LocalDate>>) : Flexible()

        data class Monthly(
            val timesPerMonth: Int,
            val preferredDays: Set<Int>,
            val scheduledPeriods : Map<LocalDate, List<LocalDate>>) : Flexible()
    }
}

data class RepeatingQuest(
    override val id: String = "",
    val name: String,
    val start: LocalDate = LocalDate.now(),
    val end: LocalDate? = null,
    val color: Color,
    val icon: Icon? = null,
    val category: Category,
    val startTime: Time? = null,
    val duration: Int,
    val reminder: Reminder? = null,
    val repeatingPattern: RepeatingPattern,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity