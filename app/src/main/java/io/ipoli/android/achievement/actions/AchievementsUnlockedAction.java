package io.ipoli.android.achievement.actions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/28/17.
 */
public class AchievementsUnlockedAction extends SimpleAchievementAction {

    public int experience;
    public int playerLevel;

    public AchievementsUnlockedAction() {
        super(Action.UNLOCK_ACHIEVEMENTS);
    }

    public AchievementsUnlockedAction(int experience, int playerLevel) {
        this();
        this.experience = experience;
        this.playerLevel = playerLevel;
    }
}
