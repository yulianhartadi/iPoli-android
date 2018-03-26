package io.ipoli.android.quest.reminder.formatter

import android.content.Context
import io.ipoli.android.R

/**
 * Created by Venelin Valkov <venelin@mypoli.fun>
 * on 10/6/17.
 */
class TimeUnitFormatter(val context: Context) {

    val customTimeUnits: List<String>
        get() {
            return context.resources.getStringArray(R.array.time_before).toList()
        }

}