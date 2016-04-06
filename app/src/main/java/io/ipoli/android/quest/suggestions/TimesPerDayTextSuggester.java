package io.ipoli.android.quest.suggestions;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;
import io.ipoli.android.quest.suggestions.providers.BaseSuggestionsProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class TimesPerDayTextSuggester extends BaseSuggestionsProvider {

    public TimesPerDayTextSuggester() {
        matcher = new TimesPerDayMatcher();
    }

    @Override
    public List<SuggestionDropDownItem> getSuggestions() {
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
