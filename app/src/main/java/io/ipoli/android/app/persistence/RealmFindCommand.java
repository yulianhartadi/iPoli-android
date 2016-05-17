package io.ipoli.android.app.persistence;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.Realm;
import io.realm.RealmObject;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/16/16.
 */
public class RealmFindCommand<T extends RealmObject & RemoteObject> {

    private RealmFindQueryBuilder<T> queryBuilder;
    private final Class<T> realmClass;

    public RealmFindCommand(RealmFindQueryBuilder<T> queryBuilder, Class<T> realmClass) {
        this.queryBuilder = queryBuilder;
        this.realmClass = realmClass;
    }

    public Observable<T> execute() {
        Realm realm = Realm.getDefaultInstance();
        return queryBuilder.buildQuery(realm.where(realmClass))
                .asObservable()
                .filter(T::isLoaded)
                .first()
                .map(realmObject -> {
                    if (!realmObject.isValid()) {
                        realm.close();
                        return null;
                    }
                    T res = realm.copyFromRealm((T) realmObject);
                    realm.close();
                    return res;
                });
    }
}
