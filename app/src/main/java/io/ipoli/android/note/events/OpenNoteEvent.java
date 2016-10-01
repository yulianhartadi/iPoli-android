package io.ipoli.android.note.events;

import io.ipoli.android.note.data.Note;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 10/1/16.
 */
public class OpenNoteEvent {
    public final Note note;

    public OpenNoteEvent(Note note) {
        this.note = note;
    }
}
