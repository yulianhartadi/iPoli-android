package io.ipoli.android.quest.events;

import io.ipoli.android.note.data.Note;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 9/29/16.
 */
public class EditNoteRequestEvent {
    public final Note note;

    public EditNoteRequestEvent(Note note) {
        this.note = note;
    }
}
