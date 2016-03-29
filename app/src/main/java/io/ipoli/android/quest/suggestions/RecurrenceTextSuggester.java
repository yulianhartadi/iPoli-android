package io.ipoli.android.quest.suggestions;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.RecurrenceMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class RecurrenceTextSuggester extends BaseTextSuggester {

    public RecurrenceTextSuggester() {
        matcher = new RecurrenceMatcher();
    }

    @Override
    public List<AddQuestSuggestion> getSuggestions() {
        int icon = R.drawable.ic_repeat_black_24dp;
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new AddQuestSuggestion(icon, "day", "every day"));
        suggestions.add(new AddQuestSuggestion(icon, "day of week", "", SuggestionType.RECURRENT_DAY_OF_WEEK));
        suggestions.add(new AddQuestSuggestion(icon, "day of month", "", SuggestionType.RECURRENT_DAY_OF_MONTH));
        return suggestions;
    }
}
