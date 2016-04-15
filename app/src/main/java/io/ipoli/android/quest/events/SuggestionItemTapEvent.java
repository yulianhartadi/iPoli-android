package io.ipoli.android.quest.events;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/16/16.
 */
public class SuggestionItemTapEvent {
    public final String suggestionText;
    public final String currentText;

    public SuggestionItemTapEvent(String suggestionText, String currentText) {
        this.suggestionText = suggestionText;
        this.currentText = currentText;
    }
}
