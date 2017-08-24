package io.ipoli.android.repeatingquest.list.usecase.data

import org.threeten.bp.LocalDate

/**
 * Created by Venelin Valkov <venelin></venelin>@curiousily.com>
 * on 8/24/17.
 */
class PeriodHistory @JvmOverloads constructor(var startDate: LocalDate?, var endDate: LocalDate?, var totalCount: Int = -1) {
    var completedCount: Int = 0
    var scheduledCount: Int = 0
        private set

    init {
        this.completedCount = 0
    }

    fun increaseCompletedCount() {
        completedCount++
        totalCount = Math.max(completedCount, totalCount)
    }

    val remainingCount: Int
        get() = totalCount - completedCount

    val remainingScheduledCount: Int
        get() = scheduledCount - completedCount

    fun increaseScheduledCount() {
        scheduledCount++
    }
}

