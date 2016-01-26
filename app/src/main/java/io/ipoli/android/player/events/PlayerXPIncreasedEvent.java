package io.ipoli.android.player.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/25/16.
 */
public class PlayerXPIncreasedEvent {
    public final int newXP;
    public final int earnedXP;
    public int currentXP;

    public PlayerXPIncreasedEvent(int currentXP, int newXP, int earnedXP) {
        this.currentXP = currentXP;
        this.newXP = newXP;
        this.earnedXP = earnedXP;
    }
}
