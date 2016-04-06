package io.ipoli.android.quest.suggestions.providers;

import java.util.List;

import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public interface SuggestionsProvider {

    List<SuggestionDropDownItem> filter(String text);
}
