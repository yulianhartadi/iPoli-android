package io.ipoli.android.player.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/2/16.
 */
public class LevelUpEvent {
    public final int newLevel;

    public LevelUpEvent(int newLevel) {
        this.newLevel = newLevel;
    }
}
