package io.ipoli.android.quest.persistence;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.gson.Gson;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.quest.data.InboxQuest;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 12/22/16.
 */
public class FirebaseInboxQuestPersistenceService extends BaseFirebasePersistenceService<InboxQuest> implements InboxQuestPersistenceService {

    public FirebaseInboxQuestPersistenceService(Bus eventBus, Gson gson) {
        super(eventBus, gson);
    }

    @Override
    protected Class<InboxQuest> getModelClass() {
        return InboxQuest.class;
    }

    @Override
    protected String getCollectionName() {
        return "inboxQuests";
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    protected GenericTypeIndicator<Map<String, InboxQuest>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, InboxQuest>>() {
        };
    }

    @Override
    protected GenericTypeIndicator<List<InboxQuest>> getGenericListIndicator() {
        return new GenericTypeIndicator<List<InboxQuest>>() {
        };
    }
}
