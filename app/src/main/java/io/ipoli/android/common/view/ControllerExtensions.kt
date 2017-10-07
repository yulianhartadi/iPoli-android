package io.ipoli.android.common.view

import android.support.annotation.ArrayRes
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat
import com.bluelinelabs.conductor.Controller

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 10/7/17.
 */
fun Controller.string(@StringRes stringRes: Int): String =
    activity!!.getString(stringRes)

fun Controller.strings(@ArrayRes stringArrayRes: Int): List<String> =
    activity!!.resources.getStringArray(stringArrayRes).toList()

fun Controller.color(@ColorRes colorRes: Int): Int =
    ContextCompat.getColor(activity!!, colorRes)