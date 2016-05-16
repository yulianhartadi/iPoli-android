package io.ipoli.android.app.persistence;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/16/16.
 */
public interface RealmFindQueryBuilder<T extends RealmObject & RemoteObject> {
    T buildQuery(RealmQuery<T> where);
}
