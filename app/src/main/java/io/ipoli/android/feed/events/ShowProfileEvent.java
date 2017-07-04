package io.ipoli.android.feed.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 7/3/17.
 */
public class ShowProfileEvent {

    public final String playerId;

    public ShowProfileEvent(String playerId) {
        this.playerId = playerId;
    }
}
