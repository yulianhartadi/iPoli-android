package com.curiousily.ipoli.models;

import com.curiousily.ipoli.events.Author;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/12/15.
 */
public class Message {
    private final String text;

    public Message(String text, Author author) {
        this.text = text;
        this.author = author;
    }

    private final Author author;

    public String getText() {
        return text;
    }

    public Author getAuthor() {
        return author;
    }
}
