package io.ipoli.android.player.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 6/5/16.
 */
public class GrowthIntervalSelectedEvent {
    public final int dayCount;

    public GrowthIntervalSelectedEvent(int dayCount) {
        this.dayCount = dayCount;
    }
}
