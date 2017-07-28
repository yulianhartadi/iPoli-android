package io.ipoli.android.achievement.actions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/28/17.
 */
public class AchievementsUnlockedAction extends SimpleAchievementAction {

    public int experience;
    public long coins;

    public AchievementsUnlockedAction() {
        super(Action.UNLOCK_ACHIEVEMENTS);
    }

    public AchievementsUnlockedAction(int experience, long coins) {
        this();
        this.experience = experience;
        this.coins = coins;
    }
}
