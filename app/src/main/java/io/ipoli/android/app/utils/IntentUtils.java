package io.ipoli.android.app.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/17/16.
 */
public class IntentUtils {

    public static PendingIntent getBroadcastPendingIntent(Context context, Intent intent) {
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
