package io.ipoli.android.achievement.persistence;

import com.couchbase.lite.Database;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import io.ipoli.android.achievement.AchievementsProgress;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class CouchbaseAchievementProgressPersistenceService
        extends BaseCouchbasePersistenceService<AchievementsProgress>
        implements AchievementProgressPersistenceService {

    public CouchbaseAchievementProgressPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        super(database, objectMapper, eventBus);
    }

    @Override
    public AchievementsProgress get() {
        return null;
    }

    @Override
    protected Class<AchievementsProgress> getModelClass() {
        return AchievementsProgress.class;
    }
}
