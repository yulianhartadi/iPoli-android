package io.ipoli.android.player.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/11/16.
 */
public class OnPlayerLevelUpEvent {
    public final int level;

    public OnPlayerLevelUpEvent(int level) {
        this.level = level;
    }
}
