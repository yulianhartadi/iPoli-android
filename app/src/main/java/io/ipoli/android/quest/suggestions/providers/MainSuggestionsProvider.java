package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.MainMatcher;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.TextEntityType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class MainSuggestionsProvider extends BaseSuggestionsProvider {
    private Set<TextEntityType> usedTypes = new HashSet<>();
    private Map<String, TextEntityType> suggestionTypePrepositions;


    public MainSuggestionsProvider() {
        matcher = new MainMatcher();
        startIdx = 0;
        suggestionTypePrepositions = new HashMap<String, TextEntityType>() {{
            put("on", TextEntityType.DUE_DATE);
            put("at", TextEntityType.START_TIME);
            put("for", TextEntityType.DURATION);
            put("every", TextEntityType.RECURRENT);
            put("times per day", TextEntityType.TIMES_PER_DAY);
        }};
    }

//    @Override
//    public SuggesterResult parse(String text) {
//        String textToParse = text.substring(startIdx);
//        Match match = matcher.match(textToParse);
//        if (match != null) {
//            SuggestionType nextSuggestionType = suggestionTypePrepositions.get(match.text.trim());
//            lastParsedText = match.text;
//            length = match.text.length();
//            startIdx = startIdx + match.start;
//            return new SuggesterResult(lastParsedText, SuggesterState.FINISH, nextSuggestionType, startIdx);
//        }
//
//        return new SuggesterResult(lastParsedText, SuggesterState.CONTINUE);
//    }

    public void addUsedTextEntityType(TextEntityType textEntityType) {
        usedTypes.add(textEntityType);
    }

    public void removeUsedTextEntityType(TextEntityType textEntityType) {
        usedTypes.remove(textEntityType);
    }

    public Set<TextEntityType> getUsedTypes() {
        return usedTypes;
    }

    @Override
    public List<SuggestionDropDownItem> getSuggestions() {
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        if (!usedTypes.contains(TextEntityType.DUE_DATE)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_event_black_18dp, "on ...", "on"));
        }
        if (!usedTypes.contains(TextEntityType.START_TIME)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_alarm_black_18dp, "at ...", "at"));
        }
        if (!usedTypes.contains(TextEntityType.DURATION)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_timer_black_18dp, "for ...", "for"));
        }
        if (!usedTypes.contains(TextEntityType.RECURRENT)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_repeat_black_24dp, "every ...", "every"));
        }
        if (!usedTypes.contains(TextEntityType.TIMES_PER_DAY)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_multiply_black_24dp, "times per day ...", "times per day"));
        }
        return suggestions;
    }
}
