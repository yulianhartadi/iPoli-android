package io.ipoli.android.chat;

import java.util.Date;
import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/16.
 */
public class Message extends RealmObject {

    @PrimaryKey
    private String id;

    @Required
    private String text;

    @Required
    private String type;

    @Required
    private Date createdAt;

    private int avatarRes;

    public Message() {}

    public Message(String text, String type, int avatarRes) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.type = type;
        this.avatarRes = avatarRes;
        this.createdAt = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getAvatarRes() {
        return avatarRes;
    }

    public void setAvatarRes(int avatarRes) {
        this.avatarRes = avatarRes;
    }

    public enum MessageType {ASSISTANT, USER}
}

