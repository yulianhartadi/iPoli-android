package io.ipoli.android.assistant.events;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/14/16.
 */
public class AvatarSelectedEvent {
    public final Integer avatarRes;

    public AvatarSelectedEvent(Integer avatarRes) {

        this.avatarRes = avatarRes;
    }
}
