package io.ipoli.android.note.data;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 9/21/16.
 */
public class Note {

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