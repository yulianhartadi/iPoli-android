package io.ipoli.android.quest.data;

import java.util.Date;

import io.ipoli.android.app.persistence.PersistedObject;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 3/26/16.
 */
public class Log extends PersistedObject {

    private String text;

    public Log() {

    }

    public Log(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean getIsDeleted() {
        return isDeleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
