package io.ipoli.android.reward.persistence;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.squareup.otto.Bus;

import java.util.List;
import java.util.Map;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.quest.persistence.OnDataChangedListener;
import io.ipoli.android.reward.data.Reward;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseRewardPersistenceService extends BaseFirebasePersistenceService<Reward> implements RewardPersistenceService {

    public FirebaseRewardPersistenceService(Bus eventBus) {
        super(eventBus);
    }

    @Override
    protected GenericTypeIndicator<Map<String, Reward>> getGenericMapIndicator() {
        return new GenericTypeIndicator<Map<String, Reward>>() {
        };
    }

    @Override
    protected GenericTypeIndicator<List<Reward>> getGenericListIndicator() {
        return new GenericTypeIndicator<List<Reward>>() {
        };
    }

    @Override
    protected DatabaseReference getCollectionReference() {
        return getPlayerReference().child(getCollectionName());
    }

    @Override
    protected Class<Reward> getModelClass() {
        return Reward.class;
    }

    @Override
    protected String getCollectionName() {
        return "rewards";
    }

    @Override
    public void findAll(OnDataChangedListener<List<Reward>> listener) {
        listenForListChange(getCollectionReference(), listener);
    }

}
