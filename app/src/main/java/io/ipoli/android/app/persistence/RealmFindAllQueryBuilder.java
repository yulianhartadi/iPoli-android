package io.ipoli.android.app.persistence;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/16/16.
 */
public interface RealmFindAllQueryBuilder<T extends RealmObject & RemoteObject> {
    RealmResults<T> buildQuery(RealmQuery<T> where);
}
