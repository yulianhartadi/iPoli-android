package io.ipoli.android.common.text

import android.content.Context
import io.ipoli.android.R

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 6/6/18.
 */
object LongFormatter {
    fun format(context: Context, value: Long): String {
        val valString = value.toString()
        if (value < 1000) {
            return valString
        }
        val main = valString.substring(0, valString.length - 3)
        var result = main
        val tail = valString[valString.length - 3]
        if (tail != '0') {
            result += ".$tail"
        }
        return context.getString(R.string.big_value_format, result)
    }
}