package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/6/16.
 */
public class DayOfMonthSuggestionsProvider implements SuggestionsProvider {

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        int icon = R.drawable.ic_event_black_18dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        suggestions.add(new SuggestionDropDownItem(icon, "21st of the month", "every 21st of the month"));
        suggestions.add(new SuggestionDropDownItem(icon, "22nd of the month", "every 22st of the month"));
        return suggestions;
    }
}