package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class TimesPerDayTextSuggestiosProvider implements SuggestionsProvider {

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        int icon = R.drawable.ic_clear_24dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        suggestions.add(new SuggestionDropDownItem(icon, "2", "2 times per day"));
        suggestions.add(new SuggestionDropDownItem(icon, "3", "3 times per day"));
        suggestions.add(new SuggestionDropDownItem(icon, "4", "4 times per day"));
        suggestions.add(new SuggestionDropDownItem(icon, "5", "5 times per day"));
        suggestions.add(new SuggestionDropDownItem(icon, "6", "6 times per day"));
        return suggestions;
    }
}
