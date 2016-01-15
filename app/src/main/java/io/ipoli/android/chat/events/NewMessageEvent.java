package io.ipoli.android.chat.events;

import io.ipoli.android.chat.Message;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/10/16.
 */
public class NewMessageEvent {
    public final Message message;

    public NewMessageEvent(Message message) {
        this.message = message;
    }
}
