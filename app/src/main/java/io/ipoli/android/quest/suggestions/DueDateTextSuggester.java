package io.ipoli.android.quest.suggestions;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.DueDateMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DueDateTextSuggester extends BaseTextSuggester {

    public DueDateTextSuggester(PrettyTimeParser parser) {
        matcher = new DueDateMatcher(parser);
    }

    @Override
    public List<AddQuestSuggestion> getSuggestions() {
        int icon = R.drawable.ic_event_black_18dp;
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new AddQuestSuggestion(icon, "today"));
        suggestions.add(new AddQuestSuggestion(icon, "tomorrow"));
        suggestions.add(new AddQuestSuggestion(icon, "12 Feb", "on 12 Feb"));
        suggestions.add(new AddQuestSuggestion(icon, "next Monday"));
        suggestions.add(new AddQuestSuggestion(icon, "after 3 days"));
        suggestions.add(new AddQuestSuggestion(icon, "in 2 months"));
        return suggestions;
    }
}
