package io.ipoli.android.quest.suggestions.providers;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/14/16.
 */
public abstract class BaseSuggestionsProvider implements SuggestionsProvider {
    protected final List<String> suggestions;
    protected List<SuggestionDropDownItem> defaultSuggestionItems = new ArrayList<>();

    public BaseSuggestionsProvider() {
        suggestions = getSuggestions();
        for(String s : suggestions) {
            defaultSuggestionItems.add(new SuggestionDropDownItem(getIcon(), s, getMatchingStartWord() + s));
        }
    }

    protected abstract List<String> getSuggestions();

    @DrawableRes
    protected abstract int getIcon();

    protected abstract String getMatchingStartWord();

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        if ((getMatchingStartWord()).contains(text.toLowerCase())) {
            return defaultSuggestionItems;
        }

        if (text.toLowerCase().startsWith(getMatchingStartWord().toLowerCase())) {
            text = text.replaceFirst(getMatchingStartWord(), "");
        }

        return applyFilters(text);
    }

    protected List<SuggestionDropDownItem> applyFilters(String text) {
        List<SuggestionDropDownItem> suggestionItems = new ArrayList<>();
        for(String s: suggestions) {
            if(s.toLowerCase().startsWith(text.toLowerCase())) {
                suggestionItems.add(new SuggestionDropDownItem(getIcon(), s, getMatchingStartWord() + s));
            }
        }
        return suggestionItems;
    }
}
