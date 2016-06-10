package io.ipoli.android.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.realm.Realm;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/27/16.
 */
public abstract class AsyncBroadcastReceiver extends BroadcastReceiver {

    private PendingResult result;

    protected Realm realm;

    @Override
    public void onReceive(Context context, Intent intent) {
        result = goAsync();
        realm = Realm.getDefaultInstance();
        doOnReceive(context, intent).subscribe(aVoid -> {
        }, throwable -> {
        }, () -> {
            if (!realm.isClosed()) {
                realm.close();
            }
            result.finish();
        });
    }

    protected abstract Observable<Void> doOnReceive(Context context, Intent intent);

}
