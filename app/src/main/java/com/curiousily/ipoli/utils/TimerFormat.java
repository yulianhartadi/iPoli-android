package com.curiousily.ipoli.utils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/12/15.
 */
public class TimerFormat {

    public static String minutesToText(int minutes) {
        return String.format("less than %dm", minutes);
    }

    public static String millisecondsToText(long milliseconds) {
        long remainingSeconds = milliseconds / 1000;
        long seconds = remainingSeconds % 60;
        long minutes = remainingSeconds / 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
