package io.ipoli.android.achievement.actions;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 7/26/17.
 */
public class WinCoinsAction extends SimpleAchievementAction {

    public long coins;

    public WinCoinsAction() {
        super(Action.WIN_COINS);
    }

    public WinCoinsAction(long coins) {
        this();
        this.coins = coins;
    }
}
