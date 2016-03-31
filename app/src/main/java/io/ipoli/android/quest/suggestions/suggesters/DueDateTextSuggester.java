package io.ipoli.android.quest.suggestions.suggesters;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DueDateTextSuggester extends BaseTextSuggester {

    public DueDateTextSuggester(PrettyTimeParser parser) {
        matcher = new DueDateMatcher(parser);
    }

    @Override
    public List<SuggestionDropDownItem> getSuggestions() {
        int icon = R.drawable.ic_event_black_18dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        suggestions.add(new SuggestionDropDownItem(icon, "today"));
        suggestions.add(new SuggestionDropDownItem(icon, "tomorrow"));
        suggestions.add(new SuggestionDropDownItem(icon, "12 Feb", "on 12 Feb"));
        suggestions.add(new SuggestionDropDownItem(icon, "next Monday"));
        suggestions.add(new SuggestionDropDownItem(icon, "after 3 days"));
        suggestions.add(new SuggestionDropDownItem(icon, "in 2 months"));
        return suggestions;
    }
}
