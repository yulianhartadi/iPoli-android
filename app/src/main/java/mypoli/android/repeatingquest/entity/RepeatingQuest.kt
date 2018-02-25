package mypoli.android.repeatingquest.entity

import mypoli.android.common.datetime.DateUtils
import mypoli.android.common.datetime.Time
import mypoli.android.quest.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.temporal.TemporalAdjusters

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
    ) : RepeatingPattern(start, end) {

        override fun nextDateWithoutRange(from: LocalDate) = from
    }

    data class Yearly(
        val dayOfMonth: Int,
        val month: Month,
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end) {

        override fun nextDateWithoutRange(from: LocalDate): LocalDate =
            LocalDate.of(from.year, month, dayOfMonth).let {
                when {
                    it.isBefore(from) -> it.plusYears(1)
                    else -> it
                }
            }
    }

    data class Weekly(
        val daysOfWeek: Set<DayOfWeek>,
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end) {

        override fun nextDateWithoutRange(from: LocalDate): LocalDate {
            require(daysOfWeek.isNotEmpty())
            var nextDate = from
            while (true) {
                if (daysOfWeek.contains(nextDate.dayOfWeek)) {
                    return nextDate
                }
                nextDate = nextDate.plusDays(1)
            }
        }
    }

    data class Monthly(
        val daysOfMonth: Set<Int>,
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end) {
        override fun nextDateWithoutRange(from: LocalDate): LocalDate {
            require(daysOfMonth.isNotEmpty())
            var nextDate = from
            while (true) {
                if (daysOfMonth.contains(nextDate.dayOfMonth)) {
                    return nextDate
                }
                nextDate = nextDate.plusDays(1)
            }
        }
    }

    sealed class Flexible(
        override val start: LocalDate,
        override val end: LocalDate?
    ) : RepeatingPattern(start, end) {

        data class Weekly(
            val timesPerWeek: Int,
            val preferredDays: Set<DayOfWeek>,
            val scheduledPeriods: Map<LocalDate, List<LocalDate>> = mapOf(),
            override val start: LocalDate = LocalDate.now(),
            override val end: LocalDate? = null
        ) : Flexible(start, end) {
            override fun nextDateWithoutRange(from: LocalDate): LocalDate {
                require(scheduledPeriods.isNotEmpty())

                val periodStart =
                    from.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))
                require(scheduledPeriods.contains(periodStart))

                val nextDate = scheduledPeriods[periodStart]!!.firstOrNull { !it.isBefore(from) }
                return nextDate ?: firstDateForNextPeriod(periodStart)
            }

            private fun firstDateForNextPeriod(periodStart: LocalDate): LocalDate {
                val nextPeriodStart = periodStart.plusWeeks(1)
                require(scheduledPeriods.contains(nextPeriodStart))
                return scheduledPeriods[nextPeriodStart]!!.first()
            }
        }

        data class Monthly(
            val timesPerMonth: Int,
            val preferredDays: Set<Int>,
            val scheduledPeriods: Map<LocalDate, List<LocalDate>> = mapOf(),
            override val start: LocalDate = LocalDate.now(),
            override val end: LocalDate? = null
        ) : Flexible(start, end) {
            override fun nextDateWithoutRange(from: LocalDate): LocalDate {
                require(scheduledPeriods.isNotEmpty())
                val periodStart = from.with(TemporalAdjusters.firstDayOfMonth())
                require(scheduledPeriods.contains(periodStart))

                val nextDate = scheduledPeriods[periodStart]!!.firstOrNull { !it.isBefore(from) }
                return nextDate ?: firstDateFromNextPeriod(periodStart)
            }

            private fun firstDateFromNextPeriod(periodStart: LocalDate): LocalDate {
                val nextPeriodStart = periodStart.plusMonths(1)
                require(scheduledPeriods.contains(nextPeriodStart))
                return scheduledPeriods[nextPeriodStart]!!.first()
            }
        }
    }

    fun nextDate(from: LocalDate) =
        when {
            end != null && from.isAfter(end) -> null
            from.isBefore(start) -> nextDateWithoutRange(start)
            else -> nextDateWithoutRange(from)
        }

    protected abstract fun nextDateWithoutRange(from: LocalDate): LocalDate
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
    val nextDate: LocalDate? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity {
    val start
        get() = repeatingPattern.start

    val end
        get() = repeatingPattern.end
}