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
public class RecurrenceSuggestionsProvider implements SuggestionsProvider {

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        int icon = R.drawable.ic_repeat_black_24dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        suggestions.add(new SuggestionDropDownItem(icon, "day", "every day"));

        if(text.length() <= "every ".length()) {
            suggestions.add(new SuggestionDropDownItem(icon, "weekday...", "every", TextEntityType.RECURRENT_DAY_OF_WEEK, true, false));
            suggestions.add(new SuggestionDropDownItem(icon, "month on...", "every", TextEntityType.RECURRENT_DAY_OF_MONTH, true, false));
        }
        return suggestions;
    }
}
