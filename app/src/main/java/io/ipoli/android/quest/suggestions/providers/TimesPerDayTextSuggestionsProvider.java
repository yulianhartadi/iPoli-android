package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class TimesPerDayTextSuggestionsProvider implements SuggestionsProvider {
    private static final int icon = R.drawable.ic_multiply_black_24dp;
    private String[] suggestions = {"2 times per day", "3 times per day", "4 times per day", "5 times per day", "6 times per day", "7 times per day"};


    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        List<SuggestionDropDownItem> suggestionItems = new ArrayList<>();
        for (String s : suggestions) {
            if (s.startsWith(text)) {
                suggestionItems.add(new SuggestionDropDownItem(icon, s));
            }
        }
        return suggestionItems;
    }
}
