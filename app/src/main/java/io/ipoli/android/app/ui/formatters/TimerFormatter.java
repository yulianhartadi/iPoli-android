package io.ipoli.android.app.ui.formatters;

import java.util.concurrent.TimeUnit;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/28/16.
 */
public class TimerFormatter {

    public static String format(long timerMillis) {
        int hours = (int) TimeUnit.MILLISECONDS.toHours(timerMillis);
        int minutes = (int) TimeUnit.MILLISECONDS.toMinutes(timerMillis) - hours * 60;
        int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(timerMillis) - hours * 3600 - minutes * 60;

        String text = String.format("%02d:%02d", minutes, seconds);
        if(hours > 0) {
            text = String.format("%d:", hours) + text;
        }

        return text;
    }
}
