package io.ipoli.android.note.data;

import com.google.firebase.database.Exclude;

import io.ipoli.android.app.persistence.PersistedObject;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/16.
 */

public class Note extends PersistedObject {

    public enum Type {TEXT, INTENT, URL}

    private String type;
    private String text;
    private String data;

    public Note() {

    }

    public Note(Type type, String text, String data) {
        this.type = type.name();
        this.text = text;
        this.data = data;
    }

    public Note(String text) {
        this.type = Type.TEXT.name();
        this.text = text;
        this.data = "";
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public Long getCreatedAt() {
        return createdAt;
    }

    public String getType() {
        return type;
    }

    @Exclude
    public Type getNoteType() {
        return Type.valueOf(type);
    }

    public String getText() {
        return text;
    }

    public String getData() {
        return data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setData(String data) {
        this.data = data;
    }
}