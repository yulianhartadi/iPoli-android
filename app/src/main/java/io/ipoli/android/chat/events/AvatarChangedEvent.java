package io.ipoli.android.chat.events;

import io.ipoli.android.chat.Message;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/14/16.
 */
public class AvatarChangedEvent {
    public final Message.MessageAuthor messageAuthor;
    public final String avatar;

    public AvatarChangedEvent(Message.MessageAuthor messageAuthor, String avatar) {
        this.messageAuthor = messageAuthor;
        this.avatar = avatar;
    }
}
