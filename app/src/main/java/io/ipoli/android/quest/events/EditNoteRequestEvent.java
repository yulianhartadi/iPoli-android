package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 9/29/16.
 */
public class EditNoteRequestEvent {
    public final String text;

    public EditNoteRequestEvent(String text) {
        this.text = text;
    }

    public EditNoteRequestEvent() {
        this.text = "";
    }
}
