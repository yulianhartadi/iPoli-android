package io.ipoli.android.quest.timer.view.formatter

import java.util.concurrent.TimeUnit

/**
 * Created by Venelin Valkov <venelin@io.ipoli.io>
 * on 01/17/2018.
 */
object TimerFormatter {

    fun format(timerMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timerMillis).toInt()
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timerMillis).toInt() - hours * 60
        val seconds =
            TimeUnit.MILLISECONDS.toSeconds(timerMillis).toInt() - hours * 3600 - minutes * 60

        var text = String.format("%02d:%02d", minutes, seconds)
        if (hours > 0) {
            text = String.format("%d:", hours) + text
        }

        return text
    }
}