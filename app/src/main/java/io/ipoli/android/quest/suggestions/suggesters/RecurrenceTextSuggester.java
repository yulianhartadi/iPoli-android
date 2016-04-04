package io.ipoli.android.quest.suggestions.suggesters;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.RecurrenceMatcher;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.SuggestionType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class RecurrenceTextSuggester extends BaseTextSuggester {

    public RecurrenceTextSuggester() {
        matcher = new RecurrenceMatcher();
    }

    @Override
    public List<SuggestionDropDownItem> getSuggestions() {
        int icon = R.drawable.ic_repeat_black_24dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        suggestions.add(new SuggestionDropDownItem(icon, "day", "every day"));
        suggestions.add(new SuggestionDropDownItem(icon, "day of week", "", SuggestionType.RECURRENT_DAY_OF_WEEK));
        suggestions.add(new SuggestionDropDownItem(icon, "day of month", "", SuggestionType.RECURRENT_DAY_OF_MONTH));
        return suggestions;
    }
}
