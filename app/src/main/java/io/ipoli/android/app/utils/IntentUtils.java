package io.ipoli.android.app.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/17/16.
 */
public class IntentUtils {

    public static PendingIntent getBroadcastPendingIntent(Context context, Intent intent, int requestCode) {
        return PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getBroadcastPendingIntent(Context context, Intent intent) {
        return getBroadcastPendingIntent(context, intent, 0);
    }

    public static boolean hasExtra(Intent intent, String extraKey) {
        return intent != null && intent.hasExtra(extraKey);
    }
}
