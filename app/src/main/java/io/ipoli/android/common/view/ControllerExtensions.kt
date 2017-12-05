package io.ipoli.android.common.view

import android.support.annotation.ArrayRes
import android.support.annotation.ColorRes
import android.support.annotation.IntegerRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import com.bluelinelabs.conductor.Controller

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/7/17.
 */
fun Controller.stringRes(@StringRes stringRes: Int): String =
    resources!!.getString(stringRes)

fun Controller.stringRes(@StringRes stringRes: Int, vararg formatArgs: Any): String =
    resources!!.getString(stringRes, *formatArgs)

fun Controller.stringsRes(@ArrayRes stringArrayRes: Int): List<String> =
    resources!!.getStringArray(stringArrayRes).toList()

fun Controller.colorRes(@ColorRes colorRes: Int): Int =
    ContextCompat.getColor(activity!!, colorRes)

fun Controller.intRes(@IntegerRes res: Int): Int =
    resources!!.getInteger(res)