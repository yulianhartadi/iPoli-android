package io.ipoli.android.app.utils;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/22/16.
 */
public class ViewUtils {
    public static int getViewRawTop(View v) {
        int[] loc = new int[2];
        v.getLocationInWindow(loc);
        return loc[1];
    }

    public static float dpToPx(float dips, Resources resources) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dips, resources.getDisplayMetrics());
    }
}
