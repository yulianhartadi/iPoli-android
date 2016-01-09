package io.ipoli.android.chat.persistence;

import android.content.Context;

import java.util.List;

import io.ipoli.android.chat.Message;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class RealmMessagePersistenceService implements MessagePersistenceService {

    private Realm realm;

    public RealmMessagePersistenceService(Context context) {
        realm = Realm.getInstance(context);
    }

    @Override
    public Message save(Message message) {
        realm.beginTransaction();
        Message realmMessage = realm.copyToRealmOrUpdate(message);
        realm.commitTransaction();
        return realmMessage;
    }

    @Override
    public List<Message> findAll() {
        return realm.copyFromRealm(realm.where(Message.class)
                .findAllSorted("createdAt", Sort.ASCENDING));
    }
}
