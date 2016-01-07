package io.ipoli.assistant.chat;

import java.util.Date;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Message {
    public String text;
    public MessageType type;
    public Date createdAt;
    public int avatarRes;

    public Message(String text, MessageType type, int avatarRes) {
        this.text = text;
        this.type = type;
        this.avatarRes = avatarRes;
    }

    public enum MessageType {ASSISTANT, USER}
}

