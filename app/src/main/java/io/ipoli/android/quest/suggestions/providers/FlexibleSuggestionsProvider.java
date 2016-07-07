package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.TextEntityType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class FlexibleSuggestionsProvider implements SuggestionsProvider {

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        int icon = R.drawable.ic_multiply_black_24dp_transparent;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
            suggestions.add(new SuggestionDropDownItem(icon, "week...", "", TextEntityType.TIMES_A_WEEK, true, false));
            suggestions.add(new SuggestionDropDownItem(icon, "month...", "", TextEntityType.TIMES_A_MONTH, true, false));
        return suggestions;
    }
}
