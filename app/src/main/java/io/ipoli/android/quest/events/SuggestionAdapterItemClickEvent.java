package io.ipoli.android.quest.events;

import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/16.
 */
public class SuggestionAdapterItemClickEvent {
    public SuggestionDropDownItem suggestionItem;

    public SuggestionAdapterItemClickEvent(SuggestionDropDownItem suggestionItem) {
        this.suggestionItem = suggestionItem;
    }
}
