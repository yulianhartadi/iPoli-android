package io.ipoli.android.player.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/11/16.
 */
public class PlayerLevelUpEvent {
    public final int newLevel;
    public final int newLevelXP;
    public final int maxXPForLevel;

    public PlayerLevelUpEvent(int newLevel, int newLevelXP, int maxXPForLevel) {
        this.newLevel = newLevel;
        this.newLevelXP = newLevelXP;
        this.maxXPForLevel = maxXPForLevel;
    }
}
