package io.ipoli.android.repeatingquest.entity

import io.ipoli.android.common.datetime.DateUtils
import io.ipoli.android.common.datetime.isBetween
import org.threeten.bp.DayOfWeek
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
                end = date.with(TemporalAdjusters.nextOrSame(DateUtils.lastDayOfWeek))
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

        override fun nextDateWithoutRange(from: LocalDate) =
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
                end = date.with(TemporalAdjusters.nextOrSame(DateUtils.lastDayOfWeek))
            )

        override val periodCount get() = daysOfWeek.size

        override fun nextDateWithoutRange(from: LocalDate): LocalDate? {
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

        override fun nextDateWithoutRange(from: LocalDate): LocalDate? {
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
                    end = date.with(TemporalAdjusters.nextOrSame(DateUtils.lastDayOfWeek))
                )

            override val periodCount get() = timesPerWeek

            override fun nextDateWithoutRange(from: LocalDate): LocalDate? {
                require(scheduledPeriods.isNotEmpty())

                val periodStart =
                    from.with(TemporalAdjusters.previousOrSame(DateUtils.firstDayOfWeek))
                require(scheduledPeriods.contains(periodStart))

                val nextDate = scheduledPeriods[periodStart]!!.firstOrNull { !it.isBefore(from) }
                return nextDate ?: firstDateForNextPeriod(periodStart)
            }

            private fun firstDateForNextPeriod(periodStart: LocalDate): LocalDate? {
                val nextPeriodStart = periodStart.plusWeeks(1)
                if (!scheduledPeriods.containsKey(nextPeriodStart)) {
                    return null
                }
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

            override fun nextDateWithoutRange(from: LocalDate): LocalDate? {
                require(scheduledPeriods.isNotEmpty())
                val periodStart = from.with(TemporalAdjusters.firstDayOfMonth())
                require(scheduledPeriods.contains(periodStart))

                val nextDate = scheduledPeriods[periodStart]!!.firstOrNull { !it.isBefore(from) }
                return nextDate ?: firstDateFromNextPeriod(periodStart)
            }

            private fun firstDateFromNextPeriod(periodStart: LocalDate): LocalDate? {
                val nextPeriodStart = periodStart.plusMonths(1)
                if (!scheduledPeriods.containsKey(nextPeriodStart)) {
                    return null
                }
                return scheduledPeriods[nextPeriodStart]!!.first()
            }
        }
    }

    abstract val periodCount: Int
    abstract fun periodRangeFor(date: LocalDate): PeriodRange
    protected abstract fun nextDateWithoutRange(from: LocalDate): LocalDate?

    fun nextDate(from: LocalDate) =
        when {
            end != null && from.isAfter(end) -> null
            from.isBefore(start) -> nextDateWithoutRange(start)
            else -> nextDateWithoutRange(from)
        }

    fun shouldScheduleOn(date: LocalDate): Boolean {
        val nextDate = nextDate(date)
        if (nextDate == null) {
            return false
        }
        return date.isEqual(nextDate)
    }

    companion object {
        fun findWeeklyPeriods(
            start: LocalDate,
            end: LocalDate,
            lastDayOfWeek: DayOfWeek
        ): List<Period> {

            val periods = mutableListOf<Period>()
            val firstDayOfWeek = lastDayOfWeek.minus(6)

            var periodStart = start.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
            val dayAfterEnd = end.plusDays(1)
            while (periodStart.isBefore(dayAfterEnd)) {
                val periodEnd = periodStart.with(TemporalAdjusters.nextOrSame(lastDayOfWeek))
                periods.add(Period(periodStart, periodEnd))
                periodStart = periodEnd.plusDays(1)
            }

            return periods
        }

        fun findMonthlyPeriods(
            start: LocalDate,
            end: LocalDate
        ): List<Period> {
            val periods = mutableListOf<Period>()

            var periodStart = start.with(TemporalAdjusters.firstDayOfMonth())
            val dayAfterEnd = end.plusDays(1)
            while (periodStart.isBefore(dayAfterEnd)) {
                val periodEnd = periodStart.with(TemporalAdjusters.lastDayOfMonth())
                periods.add(Period(periodStart, periodEnd))
                periodStart = periodEnd.plusDays(1)
            }

            return periods
        }

        fun monthlyDatesToScheduleInPeriod(
            repeatingPattern: RepeatingPattern.Monthly,
            start: LocalDate,
            end: LocalDate
        ): List<LocalDate> {

            var date = start
            val dates = mutableListOf<LocalDate>()
            while (date.isBefore(end.plusDays(1))) {
                if (date.dayOfMonth in repeatingPattern.daysOfMonth) {
                    dates.add(date)
                }
                date = date.plusDays(1)
            }
            return dates

        }

        fun weeklyDatesToScheduleInPeriod(
            repeatingPattern: RepeatingPattern.Weekly,
            start: LocalDate,
            end: LocalDate
        ): List<LocalDate> {

            var date = start
            val dates = mutableListOf<LocalDate>()
            while (date.isBefore(end.plusDays(1))) {
                if (date.dayOfWeek in repeatingPattern.daysOfWeek) {
                    dates.add(date)
                }
                date = date.plusDays(1)
            }
            return dates

        }

        fun yearlyDatesToScheduleInPeriod(
            repeatingPattern: RepeatingPattern.Yearly,
            start: LocalDate,
            end: LocalDate
        ): List<LocalDate> {
            if (start.year == end.year) {
                val date = LocalDate.of(
                    start.year,
                    repeatingPattern.month,
                    repeatingPattern.dayOfMonth
                )
                return listOf(date).filter { it.isBetween(start, end) }
            }

            var startPeriodDate = start
            val dates = mutableListOf<LocalDate>()
            while (startPeriodDate <= end) {
                val lastDayOfYear = LocalDate.of(startPeriodDate.year, 12, 31)
                val date = LocalDate.of(
                    startPeriodDate.year,
                    repeatingPattern.month,
                    repeatingPattern.dayOfMonth
                )
                val endPeriodDate = if (end.isBefore(lastDayOfYear)) end else lastDayOfYear
                if (date.isBetween(startPeriodDate, endPeriodDate)) {
                    dates.add(date)
                }
                startPeriodDate = LocalDate.of(startPeriodDate.year + 1, 1, 1)
            }
            return dates

        }
    }
}

data class PeriodRange(val start: LocalDate, val end: LocalDate)

data class PeriodProgress(val completedCount: Int, val allCount: Int)

data class Period(val start: LocalDate, val end: LocalDate)

enum class RepeatType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

val RepeatingPattern.repeatType: RepeatType
    get() = when (this) {
        is RepeatingPattern.Daily -> RepeatType.DAILY
        is RepeatingPattern.Weekly -> RepeatType.WEEKLY
        is RepeatingPattern.Flexible.Weekly -> RepeatType.WEEKLY
        is RepeatingPattern.Monthly -> RepeatType.MONTHLY
        is RepeatingPattern.Flexible.Monthly -> RepeatType.MONTHLY
        is RepeatingPattern.Yearly -> RepeatType.YEARLY
    }