package io.ipoli.android.achievement.actions;

public class SimpleAchievementAction implements AchievementAction {

    private Action action;

    public SimpleAchievementAction() {
    }

    public SimpleAchievementAction(AchievementAction.Action action) {
        this.action = action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public AchievementAction.Action getAction() {
        return action;
    }
}