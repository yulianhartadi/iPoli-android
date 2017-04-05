package io.ipoli.android.quest.suggestions.providers;

import android.support.annotation.DrawableRes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 4/14/16.
 */
public abstract class BaseSuggestionsProvider implements SuggestionsProvider {
    protected Map<String, String> suggestionToVisibleText = new LinkedHashMap<>();
    protected List<SuggestionDropDownItem> defaultSuggestionItems = new ArrayList<>();

    public BaseSuggestionsProvider() {
        for(String s : getSuggestions()) {
            suggestionToVisibleText.put(s, s);
        }
        createSuggestionItems();
    }

    protected void createSuggestionItems() {
        for(Map.Entry<String, String> entry : suggestionToVisibleText.entrySet()) {
            defaultSuggestionItems.add(new SuggestionDropDownItem(getIcon(), entry.getValue(), getMatchingStartWord() + entry.getKey()));
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
        for(Map.Entry<String, String> entry : suggestionToVisibleText.entrySet()) {
            if(entry.getKey().toLowerCase().startsWith(text.toLowerCase())) {
                suggestionItems.add(new SuggestionDropDownItem(getIcon(), entry.getValue(), getMatchingStartWord() + entry.getKey()));
            }
        }
        return suggestionItems;
    }
}
