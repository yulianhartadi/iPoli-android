package io.ipoli.android.common.text

import android.content.Context
import io.ipoli.android.R
import io.ipoli.android.repeatingquest.data.Recurrence


/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 8/24/17.
 */
object PeriodProgressFormatter {
    fun format(context: Context, remainingCount: Int, repeatType: Recurrence.RepeatType): String {
        if (remainingCount <= 0) {
            return context.getString(R.string.repeating_quest_done)
        }

        return if (repeatType === Recurrence.RepeatType.MONTHLY) {
            String.format(context.getString(R.string.repeating_quest_more_this_month), remainingCount)
        } else String.format(context.getString(R.string.repeating_quest_more_this_week), remainingCount)

    }
}