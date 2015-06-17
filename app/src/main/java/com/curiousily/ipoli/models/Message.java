package com.curiousily.ipoli.models;

import com.curiousily.ipoli.ui.events.ChangeInputEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class Message {
    public final String text;

    public Message(String text, ChangeInputEvent.Author author) {
        this.text = text;
        this.author = author;
    }

    public final ChangeInputEvent.Author author;
}
