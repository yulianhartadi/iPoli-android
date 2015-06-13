package com.curiousily.ipoli.io.event;

import com.curiousily.ipoli.models.Message;
import com.curiousily.ipoli.ui.events.ChangeInputEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class NewMessageEvent {

    private final Message message;

    public NewMessageEvent(String text, ChangeInputEvent.Author author) {
        this.message = new Message(text, author);
    }

    public Message getMessage() {
        return message;
    }
}
