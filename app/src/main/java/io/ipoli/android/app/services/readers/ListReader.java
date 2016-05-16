package io.ipoli.android.app.services.readers;

import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public interface ListReader<T> {

    Observable<T> read();
}
