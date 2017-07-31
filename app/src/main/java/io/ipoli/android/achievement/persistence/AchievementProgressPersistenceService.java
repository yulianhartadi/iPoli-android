package io.ipoli.android.achievement.persistence;

import io.ipoli.android.achievement.AchievementsProgress;
import io.ipoli.android.app.persistence.PersistenceService;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/24/17.
 */
public interface AchievementProgressPersistenceService extends PersistenceService<AchievementsProgress> {

    AchievementsProgress get();

    void save(AchievementsProgress achievementsProgress);

}
