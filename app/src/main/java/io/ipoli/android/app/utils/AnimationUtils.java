package io.ipoli.android.app.utils;

import android.support.annotation.IntegerRes;
import android.view.View;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/1/17.
 */
public class AnimationUtils {

    public static void fadeIn(View view, @IntegerRes int duration, long startDelay) {
        view.animate().alpha(1f).setStartDelay(startDelay).setDuration(view.getResources().getInteger(duration)).start();
    }

    public static void fadeIn(View view, long startDelay) {
        fadeIn(view, android.R.integer.config_mediumAnimTime, startDelay);
    }

    public static void fadeIn(View view) {
        fadeIn(view, android.R.integer.config_mediumAnimTime, 0);
    }

    public static void fadeOut(View view) {
        view.animate().alpha(0f).setDuration(view.getResources().getInteger(android.R.integer.config_shortAnimTime)).start();
    }
}
