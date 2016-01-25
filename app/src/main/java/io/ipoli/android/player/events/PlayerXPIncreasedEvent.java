package io.ipoli.android.player.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/25/16.
 */
public class PlayerXPIncreasedEvent {
    public final int newXP;
    public int currentXP;

    public PlayerXPIncreasedEvent(int currentXP, int newXP) {
        this.currentXP = currentXP;
        this.newXP = newXP;
    }
}
