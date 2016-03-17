package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/17/16.
 */
public class PlayerXPDecreasedEvent {
    public final int currentXP;
    public final int newXP;
    public final int earnedXP;

    public PlayerXPDecreasedEvent(int currentXP, int newXP, int earnedXP) {
        this.currentXP = currentXP;
        this.newXP = newXP;
        this.earnedXP = earnedXP;
    }
}
