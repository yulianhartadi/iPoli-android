package io.ipoli.android.achievement.actions;

public class SimpleAchievementAction implements AchievementAction {

    private final Action action;

    public SimpleAchievementAction(AchievementAction.Action action) {
        this.action = action;
    }

    @Override
    public AchievementAction.Action getAction() {
        return action;
    }
}