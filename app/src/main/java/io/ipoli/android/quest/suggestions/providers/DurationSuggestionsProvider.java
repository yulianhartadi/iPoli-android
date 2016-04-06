package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DurationSuggestionsProvider extends BaseSuggestionsProvider {

    public DurationSuggestionsProvider() {
        matcher = new DurationMatcher();
    }

    @Override
    public List<SuggestionDropDownItem> getSuggestions() {
        int icon = R.drawable.ic_timer_black_18dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        suggestions.add(new SuggestionDropDownItem(icon, "15 min", "for 15 min"));
        suggestions.add(new SuggestionDropDownItem(icon, "30 min", "for 30 min"));
        suggestions.add(new SuggestionDropDownItem(icon, "1 hour", "for 1 hour"));
        suggestions.add(new SuggestionDropDownItem(icon, "1h and 30m", "for 1h and 30m"));
        return suggestions;
    }
}
