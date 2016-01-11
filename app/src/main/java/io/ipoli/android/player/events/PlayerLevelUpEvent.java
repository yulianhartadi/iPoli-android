package io.ipoli.android.player.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/11/16.
 */
public class PlayerLevelUpEvent {
    public final int level;

    public PlayerLevelUpEvent(int level) {
        this.level = level;
    }
}
