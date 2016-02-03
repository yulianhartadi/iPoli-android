package io.ipoli.android.assistant.persistence;

import android.content.Context;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.assistant.Assistant;
import io.realm.Realm;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/9/16.
 */
public class RealmAssistantPersistenceService implements AssistantPersistenceService {

    private final Context context;
    private Realm realm;

    public RealmAssistantPersistenceService(Context context) {
        this.context = context;
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
            return new Assistant(context.getString(R.string.default_assistant_name), Constants.DEFAULT_ASSISTANT_AVATAR, Assistant.State.NORMAL);
        }
        return realm.copyFromRealm(assistant);
    }
}
