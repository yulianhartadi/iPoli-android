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
        override fun periodRangeFor(date: LocalDate) =
            PeriodRange(
                start = date.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek)),
                end = date.with(DateUtils.lastDayOfWeek)
            )

        override val periodCount get() = DayOfWeek.values().size

        override fun nextDateWithoutRange(from: LocalDate) = from
    }

    data class Yearly(
        val dayOfMonth: Int,
        val month: Month,
        override val start: LocalDate = LocalDate.now(),
        override val end: LocalDate? = null
    ) : RepeatingPattern(start, end) {
        override fun periodRangeFor(date: LocalDate) =
            PeriodRange(
                start = date.with(TemporalAdjusters.firstDayOfYear()),
                end = date.with(TemporalAdjusters.lastDayOfYear())
            )

        override val periodCount get() = 1

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
        override fun periodRangeFor(date: LocalDate) =
            PeriodRange(
                start = date.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek)),
                end = date.with(DateUtils.lastDayOfWeek)
            )

        override val periodCount get() = daysOfWeek.size

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
        override fun periodRangeFor(date: LocalDate) =
            PeriodRange(
                start = date.with(TemporalAdjusters.firstDayOfMonth()),
                end = date.with(TemporalAdjusters.lastDayOfMonth())
            )

        override val periodCount get() = daysOfMonth.size

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
            override fun periodRangeFor(date: LocalDate) =
                PeriodRange(
                    start = date.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek)),
                    end = date.with(DateUtils.lastDayOfWeek)
                )

            override val periodCount get() = timesPerWeek

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
            override fun periodRangeFor(date: LocalDate) =
                PeriodRange(
                    start = date.with(TemporalAdjusters.firstDayOfMonth()),
                    end = date.with(TemporalAdjusters.lastDayOfMonth())
                )

            override val periodCount get() = timesPerMonth

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

    abstract val periodCount: Int
    abstract fun periodRangeFor(date: LocalDate): PeriodRange
    protected abstract fun nextDateWithoutRange(from: LocalDate): LocalDate
}

data class PeriodRange(val start: LocalDate, val end: LocalDate)

data class PeriodProgress(val completedCount: Int, val allCount: Int)

enum class FrequencyType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

val RepeatingPattern.frequencyType: FrequencyType
    get() = when (this) {
        is RepeatingPattern.Daily -> FrequencyType.DAILY
        is RepeatingPattern.Weekly -> FrequencyType.WEEKLY
        is RepeatingPattern.Flexible.Weekly -> FrequencyType.WEEKLY
        is RepeatingPattern.Monthly -> FrequencyType.MONTHLY
        is RepeatingPattern.Flexible.Monthly -> FrequencyType.MONTHLY
        is RepeatingPattern.Yearly -> FrequencyType.YEARLY
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
    val periodProgress: PeriodProgress? = null,
    override val createdAt: Instant = Instant.now(),
    override val updatedAt: Instant = Instant.now()
) : Entity {
    val start
        get() = repeatingPattern.start

    val end
        get() = repeatingPattern.end

    val isCompleted
        get() = if (end == null) false else LocalDate.now().isAfter(end)
}