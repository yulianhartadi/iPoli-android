package io.ipoli.android.quest.suggestions;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.StartTimeMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class StartTimeTextSuggester extends BaseTextSuggester {

    public StartTimeTextSuggester(PrettyTimeParser parser) {
        matcher = new StartTimeMatcher(parser);
    }

    @Override
    public List<AddQuestSuggestion> getSuggestions() {
        int icon = R.drawable.ic_alarm_black_18dp;
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        suggestions.add(new AddQuestSuggestion(icon, "19:30", "at 19:30"));
        suggestions.add(new AddQuestSuggestion(icon, "7 pm", "at 7 pm"));
        suggestions.add(new AddQuestSuggestion(icon, "12:00", "at 12:00"));
        suggestions.add(new AddQuestSuggestion(icon, "22:00", "at 22:00"));
        return suggestions;
    }
}
