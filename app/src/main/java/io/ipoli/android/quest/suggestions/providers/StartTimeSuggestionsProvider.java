package io.ipoli.android.quest.suggestions.providers;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class StartTimeSuggestionsProvider extends BaseSuggestionsProvider {

    public StartTimeSuggestionsProvider(PrettyTimeParser parser) {
        matcher = new StartTimeMatcher(parser);
    }

    @Override
    public List<SuggestionDropDownItem> getSuggestions() {
        int icon = R.drawable.ic_alarm_black_18dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        suggestions.add(new SuggestionDropDownItem(icon, "19:30", "at 19:30"));
        suggestions.add(new SuggestionDropDownItem(icon, "7 pm", "at 7 pm"));
        suggestions.add(new SuggestionDropDownItem(icon, "12:00", "at 12:00"));
        suggestions.add(new SuggestionDropDownItem(icon, "22:00", "at 22:00"));
        return suggestions;
    }
}
