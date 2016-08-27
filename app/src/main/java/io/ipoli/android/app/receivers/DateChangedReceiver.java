package io.ipoli.android.app.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.squareup.otto.Bus;

import org.joda.time.LocalDate;

import javax.inject.Inject;

import io.ipoli.android.app.App;
import io.ipoli.android.app.events.DateChangedEvent;
import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.app.utils.IntentUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/16.
 */
public class DateChangedReceiver extends BroadcastReceiver {

    public static final String ACTION_DATE_CHANGED = "io.ipoli.android.intent.action.DATE_CHANGED";

    @Inject
    Bus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        eventBus.post(new DateChangedEvent());


    }
}
