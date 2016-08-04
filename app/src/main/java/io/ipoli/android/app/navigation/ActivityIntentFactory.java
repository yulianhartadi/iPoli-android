package io.ipoli.android.app.navigation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;

import io.ipoli.android.app.activities.BaseActivity;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/4/16.
 */
public class ActivityIntentFactory {

    public static PendingIntent createWithParentStack(Class<? extends BaseActivity> activityClass, Intent intent, Context context) {
        return createWithParentStack(activityClass, intent, context, 0);
    }

    public static PendingIntent createWithParentStack(Class<? extends BaseActivity> activityClass, Intent intent, Context context, int requestCode) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(intent);

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Intent[] createIntentWithParentStack(Class<? extends BaseActivity> activityClass, Intent intent, Context context) {
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getIntents();
    }
}
