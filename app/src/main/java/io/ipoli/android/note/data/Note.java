package io.ipoli.android.note.data;

import io.ipoli.android.app.persistence.PersistedObject;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/16.
 */

public class Note extends PersistedObject {

    public enum NoteType {TEXT, INTENT, URL}

    private String noteType;
    private String text;
    private String data;

    public Note() {

    }

    public Note(NoteType noteType, String text, String data) {
        this.noteType = noteType.name();
        this.text = text;
        this.data = data;
    }

    public Note(String text) {
        this.noteType = NoteType.TEXT.name();
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

    public String getNoteType() {
        return noteType;
    }


    public NoteType getNoteTypeValue() {
        return NoteType.valueOf(noteType);
    }

    public String getText() {
        return text;
    }

    public String getData() {
        return data;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setData(String data) {
        this.data = data;
    }
}