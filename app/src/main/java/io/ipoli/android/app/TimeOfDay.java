package io.ipoli.android.app;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/13/16.
 */
public enum TimeOfDay {
    MORNING, AFTERNOON, EVENING, ANY_TIME;

    public static int getNameRes(TimeOfDay timeOfDay) {
        switch (timeOfDay) {
            case MORNING:
                return R.string.morning;
            case AFTERNOON:
                return R.string.afternoon;
            case EVENING:
                return R.string.evening;
            default:
                return R.string.any_time;
        }
    }
}
