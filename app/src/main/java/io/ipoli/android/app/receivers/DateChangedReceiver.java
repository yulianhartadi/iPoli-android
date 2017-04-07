package io.ipoli.android.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.app.App;
import io.ipoli.android.app.events.DateChangedEvent;
import io.ipoli.android.player.persistence.PlayerPersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 8/27/16.
 */
public class DateChangedReceiver extends BroadcastReceiver {

    public static final String ACTION_DATE_CHANGED = "io.ipoli.android.intent.action.DATE_CHANGED";

    @Inject
    Bus eventBus;

    @Inject
    PlayerPersistenceService playerPersistenceService;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        if (playerPersistenceService.get() == null) {
            return;
        }
        eventBus.post(new DateChangedEvent());
    }
}
