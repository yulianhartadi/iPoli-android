package io.ipoli.android.app.persistence;

import java.util.List;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/16/16.
 */
public class RealmFindAllCommand<T extends RealmObject & RemoteObject> {

    private RealmFindAllQueryBuilder<T> queryBuilder;
    private final RealmQuery<T> query;
    private final Realm realm;

    public RealmFindAllCommand(RealmFindAllQueryBuilder<T> queryBuilder, RealmQuery<T> query, Realm realm) {
        this.queryBuilder = queryBuilder;
        this.query = query;
        this.realm = realm;
    }

    public List<T> execute() {
        realm.beginTransaction();
        List<T> result = realm.copyFromRealm(queryBuilder.buildQuery(query));
        realm.commitTransaction();
        return result;
    }
}
