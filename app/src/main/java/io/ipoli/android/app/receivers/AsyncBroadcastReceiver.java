package io.ipoli.android.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/27/16.
 */
public abstract class AsyncBroadcastReceiver extends BroadcastReceiver {

    private PendingResult result;

    @Override
    public void onReceive(Context context, Intent intent) {
        result = goAsync();
        doOnReceive(context, intent).subscribe(aVoid -> {
        }, throwable -> {
        }, () -> {
            result.finish();
        });
    }

    protected abstract Observable<Void> doOnReceive(Context context, Intent intent);

}
