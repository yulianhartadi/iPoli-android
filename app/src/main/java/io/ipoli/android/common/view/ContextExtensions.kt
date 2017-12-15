package io.ipoli.android.common.view

import android.content.Context
import android.preference.PreferenceManager
import android.support.v7.view.ContextThemeWrapper
import io.ipoli.android.Constants

/**
 * Created by Venelin Valkov <venelin@ipoli.io>
 * on 15.12.17.
 */

val Context.playerTheme: Int
    get() {
        val pm = PreferenceManager.getDefaultSharedPreferences(this)
        if (pm.contains(Constants.KEY_THEME)) {
            val themeName = pm.getString(Constants.KEY_THEME, "")
            if (themeName.isNotEmpty()) {
                return AndroidTheme.valueOf(themeName).style
            }
        }
        return Constants.DEFAULT_THEME.style
    }

fun Context.asThemedWrapper() =
    ContextThemeWrapper(this, playerTheme)