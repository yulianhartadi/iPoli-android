package io.ipoli.android.assistant.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/14/16.
 */
public class OnAvatarSelectedEvent {
    public final Integer avatarRes;

    public OnAvatarSelectedEvent(Integer avatarRes) {

        this.avatarRes = avatarRes;
    }
}
