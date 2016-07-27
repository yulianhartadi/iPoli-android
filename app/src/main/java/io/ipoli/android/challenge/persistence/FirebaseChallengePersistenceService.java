package io.ipoli.android.challenge.persistence;

import android.content.Context;

import com.squareup.otto.Bus;

import java.util.List;

import io.ipoli.android.app.persistence.BaseFirebasePersistenceService;
import io.ipoli.android.challenge.data.Challenge;
import io.ipoli.android.quest.persistence.OnDatabaseChangedListener;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/27/16.
 */
public class FirebaseChallengePersistenceService extends BaseFirebasePersistenceService<Challenge> implements ChallengePersistenceService {

    public FirebaseChallengePersistenceService(Context context, Bus eventBus) {
        super(context, eventBus);
    }

    @Override
    protected Class<Challenge> getModelClass() {
        return Challenge.class;
    }

    @Override
    protected String getCollectionName() {
        return "challenges";
    }

    @Override
    public void findAllNotCompleted(OnDatabaseChangedListener<List<Challenge>> listener) {

    }

    @Override
    public List<Challenge> findAllNotCompleted() {
        return null;
    }

}
