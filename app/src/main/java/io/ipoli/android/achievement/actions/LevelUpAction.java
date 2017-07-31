package io.ipoli.android.achievement.actions;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/25/17.
 */
public class LevelUpAction extends SimpleAchievementAction {

    public int level;

    public LevelUpAction() {
        super(Action.LEVEL_UP);
    }

    public LevelUpAction(int level) {
        this();
        this.level = level;
    }
}
