package io.ipoli.android.achievement.events;

import java.util.List;

import io.ipoli.android.achievement.Achievement;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/28/17.
 */
public class AchievementsUnlockedEvent {
    public final List<Achievement> achievements;

    public AchievementsUnlockedEvent(List<Achievement> achievements) {
        this.achievements = achievements;
    }
}
