package io.ipoli.android.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ipoli.android.app.App;
import io.ipoli.android.app.events.NoNetworkConnectionEvent;
import io.ipoli.android.app.utils.NetworkConnectivityUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {
   @Inject
    Bus eventBus;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.getAppComponent(context).inject(this);
        if(!NetworkConnectivityUtils.isConnectedToInternet(context)) {
            eventBus.post(new NoNetworkConnectionEvent());
        }
    }
}
