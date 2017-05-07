package io.ipoli.android.app;

import android.content.Context;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 12/13/16.
 */

public enum TimeOfDay {
    MORNING, AFTERNOON, EVENING, ANY_TIME;

    public static String getLocalName(Context context, TimeOfDay timeOfDay) {
        switch (timeOfDay) {
            case MORNING:
                return context.getString(R.string.morning);
            case AFTERNOON:
                return context.getString(R.string.afternoon);
            case EVENING:
                return context.getString(R.string.evening);
            default:
                return context.getString(R.string.any_time);
        }
    }
}
