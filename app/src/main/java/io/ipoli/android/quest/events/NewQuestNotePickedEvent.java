package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 1/14/17.
 */
public class NewQuestNotePickedEvent {
    public final String text;

    public NewQuestNotePickedEvent(String text) {
        this.text = text;
    }
}
