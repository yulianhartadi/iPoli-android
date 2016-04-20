package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class NewQuestSavedEvent {
    public final String text;
    public final String source;

    public NewQuestSavedEvent(String text, String source) {
        this.text = text;
        this.source = source;
    }
}
