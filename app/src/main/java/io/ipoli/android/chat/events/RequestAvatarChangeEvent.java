package io.ipoli.android.chat.events;

import io.ipoli.android.chat.Message;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/14/16.
 */
public class RequestAvatarChangeEvent {
    public final Message.MessageAuthor messageAuthor;

    public RequestAvatarChangeEvent(Message.MessageAuthor messageAuthor) {
        this.messageAuthor = messageAuthor;
    }
}
