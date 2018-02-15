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
    data class Weekly(val daysOfWeek: Set<DayOfWeek>, val flexibleCount: Int = 0) :
        RepeatingPattern()

    data class Monthly(val daysOfMonth: Set<Int>, val flexibleCount: Int = 0) :
        RepeatingPattern()
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