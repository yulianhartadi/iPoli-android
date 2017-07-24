package io.ipoli.android.achievement.persistence;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.otto.Bus;

import io.ipoli.android.Constants;
import io.ipoli.android.achievement.AchievementsProgress;
import io.ipoli.android.app.persistence.BaseCouchbasePersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public class CouchbaseAchievementProgressPersistenceService
        extends BaseCouchbasePersistenceService<AchievementsProgress>
        implements AchievementProgressPersistenceService {

    private final View progressView;

    public CouchbaseAchievementProgressPersistenceService(Database database, ObjectMapper objectMapper, Bus eventBus) {
        super(database, objectMapper, eventBus);

        progressView = database.getView("achievement/progress");
        if (progressView.getMap() == null) {
            progressView.setMap((document, emitter) -> {
                String type = (String) document.get("type");
                if (AchievementsProgress.TYPE.equals(type)) {
                    emitter.emit(document.get("_id"), document);
                }
            }, Constants.DEFAULT_VIEW_VERSION);
        }
    }

    @Override
    public AchievementsProgress get() {
        Query query = progressView.createQuery();
        try {
            QueryEnumerator enumerator = query.run();
            if (enumerator.hasNext()) {
                QueryRow row = enumerator.next();
                return toObject(row.getValue());
            }
        } catch (CouchbaseLiteException e) {
            postError(e);
        }
        return null;
    }

    @Override
    protected Class<AchievementsProgress> getModelClass() {
        return AchievementsProgress.class;
    }
}
