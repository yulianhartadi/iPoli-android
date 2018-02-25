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

sealed class RepeatingPattern(
    open val start: LocalDate,
    open val end: LocalDate?
) {
    data class Daily(
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end)

    data class Yearly(
        val dayOfMonth: Int,
        val month: Int,
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end)

    data class Weekly(
        val daysOfWeek: Set<DayOfWeek>,
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end)

    data class Monthly(
        val daysOfMonth: Set<Int>,
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end)

    sealed class Flexible(
        override val start: LocalDate,
        override val end: LocalDate?
    ) : RepeatingPattern(start, end) {

        data class Weekly(
            val timesPerWeek: Int,
            val preferredDays: Set<DayOfWeek>,
            val scheduledPeriods : Map<LocalDate, List<LocalDate>> = mapOf(),
            override val start: LocalDate = LocalDate.now(),
            override val end: LocalDate? = null
        ) : Flexible(start, end)

        data class Monthly(
            val timesPerMonth: Int,
            val preferredDays: Set<Int>,
            val scheduledPeriods : Map<LocalDate, List<LocalDate>> = mapOf(),
            override val start: LocalDate = LocalDate.now(),
            override val end: LocalDate? = null
        ) : Flexible(start, end)
    }
}

data class RepeatingQuest(
    override val id: String = "",
    val name: String,
    val color: Color,
    val icon: Icon? = null,
    val category: Category,
    val startTime: Time? = null,
    val duration: Int,
    val reminder: Reminder? = null,
    val repeatingPattern: RepeatingPattern,
    val nextDate : LocalDate? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity {
    val start
        get() = repeatingPattern.start

    val end
        get() = repeatingPattern.end
}