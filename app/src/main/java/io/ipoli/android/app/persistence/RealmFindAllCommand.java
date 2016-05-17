package io.ipoli.android.app.persistence;

import java.util.List;

import io.ipoli.android.app.net.RemoteObject;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import rx.Observable;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/16/16.
 */
public class RealmFindAllCommand<T extends RealmObject & RemoteObject> {

    private final Class<T> realmClass;
    private RealmFindAllQueryBuilder<T> queryBuilder;

    public RealmFindAllCommand(RealmFindAllQueryBuilder<T> queryBuilder, Class<T> realmClass) {
        this.realmClass = realmClass;
        this.queryBuilder = queryBuilder;
    }

    public Observable<List<T>> execute() {
        Realm realm = Realm.getDefaultInstance();
        return queryBuilder.buildQuery(realm.where(realmClass))
                .asObservable()
                .filter(RealmResults::isLoaded)
                .first()
                .map(results -> {
                    List<T> res = realm.copyFromRealm(results);
                    realm.close();
                    return res;
                });
    }
}
