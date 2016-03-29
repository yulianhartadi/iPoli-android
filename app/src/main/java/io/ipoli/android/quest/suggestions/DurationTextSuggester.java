package io.ipoli.android.quest.suggestions;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.DurationMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DurationTextSuggester extends BaseTextSuggester {

    public DurationTextSuggester() {
        matcher = new DurationMatcher();
    }

    @Override
    public List<AddQuestSuggestion> getSuggestions() {
        int icon = R.drawable.ic_timer_black_18dp;
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new AddQuestSuggestion(icon, "15 min", "for 15 min"));
        suggestions.add(new AddQuestSuggestion(icon, "30 min", "for 30 min"));
        suggestions.add(new AddQuestSuggestion(icon, "1 hour", "for 1 hour"));
        suggestions.add(new AddQuestSuggestion(icon, "1h and 30m", "for 1h and 30m"));
        return suggestions;
    }
}
