package io.ipoli.android.app.utils;

import android.content.Context;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/14/16.
 */
public class ResourceUtils {
    public static int extractDrawableResource(Context context, String name) {
        return context.getResources().getIdentifier(name, "drawable", context.getPackageName());
    }

    public static String extractDrawableName(Context context, int resource) {
        return context.getResources().getResourceEntryName(resource);
    }
}
