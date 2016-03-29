package io.ipoli.android.quest.events;

import io.ipoli.android.quest.suggestions.AddQuestSuggestion;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/16.
 */
public class SuggestionAdapterItemClickEvent {
    public AddQuestSuggestion suggestionItem;

    public SuggestionAdapterItemClickEvent(AddQuestSuggestion suggestionItem) {
        this.suggestionItem = suggestionItem;
    }
}
