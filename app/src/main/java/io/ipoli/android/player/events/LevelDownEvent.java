package io.ipoli.android.player.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/2/16.
 */
public class LevelDownEvent {
    public final int newLevel;

    public LevelDownEvent(int newLevel) {
        this.newLevel = newLevel;
    }
}
