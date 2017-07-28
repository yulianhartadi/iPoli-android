package io.ipoli.android.achievement.actions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/28/17.
 */
public class IsFollowedAction extends SimpleAchievementAction {

    public int followersCount;

    public IsFollowedAction() {
        super(Action.IS_FOLLOWED);
    }

    public IsFollowedAction(int followersCount) {
        this();
        this.followersCount = followersCount;
    }
}
