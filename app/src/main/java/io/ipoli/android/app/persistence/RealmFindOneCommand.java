package io.ipoli.android.app.persistence;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/16/16.
 */
public class RealmFindOneCommand<T extends RealmObject & RemoteObject> {

    private RealmFindOneQueryBuilder<T> queryBuilder;
    private final RealmQuery<T> query;
    private final Realm realm;

    public RealmFindOneCommand(RealmFindOneQueryBuilder<T> queryBuilder, RealmQuery<T> query, Realm realm) {
        this.queryBuilder = queryBuilder;
        this.query = query;
        this.realm = realm;
    }

    public T execute() {
        realm.beginTransaction();
        T result = null;
        T realmResult = queryBuilder.buildQuery(query);
        if (realmResult != null) {
            result = realm.copyFromRealm(realmResult);
        }
        realm.commitTransaction();
        return result;
    }
}
