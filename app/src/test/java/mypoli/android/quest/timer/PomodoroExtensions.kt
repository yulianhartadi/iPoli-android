package mypoli.android.quest.timer

import mypoli.android.Constants

/**
 * Created by Polina Zhelyazkova <polina@mypoli.fun>
 * on 1/18/18.
 */
fun Int.pomodoros() =
    this * Constants.DEFAULT_POMODORO_WORK_DURATION

fun Int.shortBreaks() =
    this * Constants.DEFAULT_POMODORO_BREAK_DURATION

fun Int.longBreaks() =
    this * Constants.DEFAULT_POMODORO_LONG_BREAK_DURATION