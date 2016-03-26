package io.ipoli.android.quest.events;

import io.ipoli.android.quest.AddQuestSuggestion;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/24/16.
 */
public class SuggestionAdapterItemClickEvent {
    public AddQuestSuggestion suggestionItem;

    public SuggestionAdapterItemClickEvent(AddQuestSuggestion suggestionItem) {
        this.suggestionItem = suggestionItem;
    }
}
