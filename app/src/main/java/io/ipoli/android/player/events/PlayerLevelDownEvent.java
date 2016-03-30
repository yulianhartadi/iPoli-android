package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/17/16.
 */
public class PlayerLevelDownEvent {
    public final int newLevel;
    public final int newLevelXP;
    public final int maxXPForLevel;
    public final int earnedXP;

    public PlayerLevelDownEvent(int newLevel, int newLevelXP, int maxXPForLevel, int earnedXP) {
        this.newLevel = newLevel;
        this.newLevelXP = newLevelXP;
        this.maxXPForLevel = maxXPForLevel;
        this.earnedXP = earnedXP;
    }
}
