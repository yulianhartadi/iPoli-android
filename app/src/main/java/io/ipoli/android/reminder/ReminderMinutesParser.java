package io.ipoli.android.reminder;

import android.support.v4.util.Pair;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/30/16.
 */
public class ReminderMinutesParser {

    public static Pair<Long, TimeOffsetType> parseCustomMinutes(long minutes) {
        if(minutes == 0) {
            return new Pair<>(minutes, TimeOffsetType.MINUTES);
        }

        TimeOffsetType[] types = TimeOffsetType.values();
        for (int i = types.length - 1; i >= 0; i--) {
            TimeOffsetType type = types[i];
            if (minutes % type.getMinutes() == 0) {
                return new Pair<>(minutes / type.getMinutes(), type);
            }
        }

        return null;
    }
}
