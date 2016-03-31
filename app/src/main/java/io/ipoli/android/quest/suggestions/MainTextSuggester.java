package io.ipoli.android.quest.suggestions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.R;
import io.ipoli.android.quest.parsers.MainMatcher;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class MainTextSuggester extends BaseTextSuggester {
    private Set<SuggestionType> usedTypes = new HashSet<>();
    private Map<String, SuggestionType> suggestionTypePrepositions;


    public MainTextSuggester() {
        matcher = new MainMatcher();
        startIdx = 0;
        suggestionTypePrepositions = new HashMap<String, SuggestionType>() {{
            put("on", SuggestionType.DUE_DATE);
            put("at", SuggestionType.START_TIME);
            put("for", SuggestionType.DURATION);
            put("every", SuggestionType.RECURRENT);
            put("times per day", SuggestionType.TIMES_PER_DAY);
        }};
    }

    @Override
    public SuggesterResult parse(String text) {
        String textToParse = text.substring(startIdx);
        String match = matcher.match(textToParse);
        if (!match.isEmpty()) {
            SuggestionType nextSuggestionType = suggestionTypePrepositions.get(match.trim());
            lastParsedText = match;
            length = match.length();
            startIdx = startIdx + textToParse.indexOf(match);
            return new SuggesterResult(lastParsedText, SuggesterState.FINISH, nextSuggestionType, startIdx);
        }

        return new SuggesterResult(lastParsedText, SuggesterState.CONTINUE);
    }

    public void addUsedSuggestionType(SuggestionType suggestionType) {
        usedTypes.add(suggestionType);
    }

    public void removeUsedSuggestionType(SuggestionType suggestionType) {
        usedTypes.remove(suggestionType);
    }

    @Override
    public List<AddQuestSuggestion> getSuggestions() {
        List<AddQuestSuggestion> suggestions = new ArrayList<>();
        if (!usedTypes.contains(SuggestionType.DUE_DATE)) {
            suggestions.add(new AddQuestSuggestion(R.drawable.ic_event_black_18dp, "on ...", "on", SuggestionType.DUE_DATE));
        }
        if (!usedTypes.contains(SuggestionType.START_TIME)) {
            suggestions.add(new AddQuestSuggestion(R.drawable.ic_alarm_black_18dp, "at ...", "at", SuggestionType.START_TIME));
        }
        if (!usedTypes.contains(SuggestionType.DURATION)) {
            suggestions.add(new AddQuestSuggestion(R.drawable.ic_timer_black_18dp, "for ...", "for", SuggestionType.DURATION));
        }
        if (!usedTypes.contains(SuggestionType.RECURRENT)) {
            suggestions.add(new AddQuestSuggestion(R.drawable.ic_repeat_black_24dp, "every ...", "every", SuggestionType.RECURRENT));
        }
        if (!usedTypes.contains(SuggestionType.TIMES_PER_DAY)) {
            suggestions.add(new AddQuestSuggestion(R.drawable.ic_multiply_black_24dp, "times per day ...", "times per day", SuggestionType.TIMES_PER_DAY));
        }
        return suggestions;
    }
}
