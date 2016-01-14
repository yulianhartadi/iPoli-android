package io.ipoli.android.assistant.persistence;

import android.content.Context;

import io.ipoli.android.assistant.Assistant;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class RealmAssistantPersistenceService implements AssistantPersistenceService {

    private Realm realm;

    public RealmAssistantPersistenceService(Context context) {
        realm = Realm.getInstance(context);
    }

    @Override
    public Assistant save(Assistant assistant) {
        realm.beginTransaction();
        Assistant realmAssistant = realm.copyFromRealm(realm.copyToRealmOrUpdate(assistant));
        realm.commitTransaction();
        return realmAssistant;
    }

    @Override
    public Assistant find() {
        Assistant assistant = realm.where(Assistant.class).findFirst();
        if (assistant == null) {
            return new Assistant("iPoli", "avatar_01", Assistant.State.TUTORIAL_START);
        }
        return realm.copyFromRealm(assistant);
    }
}
