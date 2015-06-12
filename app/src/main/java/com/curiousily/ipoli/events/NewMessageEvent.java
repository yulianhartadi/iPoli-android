package com.curiousily.ipoli.events;

import com.curiousily.ipoli.models.Message;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class NewMessageEvent {

    private final Message message;

    public NewMessageEvent(String text, Author author) {
        this.message = new Message(text, author);
    }

    public Message getMessage() {
        return message;
    }
}
