package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.MatcherType;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.TextEntityType;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class MainSuggestionsProvider implements SuggestionsProvider {
    private Set<MatcherType> usedTypes = new HashSet<>();

    public void addUsedMatcherType(MatcherType matcherType) {
        usedTypes.add(matcherType);
    }

    public void removeUsedMatcherType(MatcherType matcherType) {
        usedTypes.remove(matcherType);
    }

    public Set<MatcherType> getUsedTypes() {
        return usedTypes;
    }

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();

        if (!usedTypes.contains(MatcherType.DATE)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_event_black_18dp, "on ...", "on"));
        }

        if (!usedTypes.contains(MatcherType.TIME)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_clock_black_24dp, "at ...", "at"));
        }
        if (!usedTypes.contains(MatcherType.DURATION)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_timer_black_18dp, "for ...", "for"));
        }
        if (!usedTypes.contains(MatcherType.DATE)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_repeat_black_24dp, "every ...", "every"));
        }
        if (!usedTypes.contains(MatcherType.TIME)) {
            suggestions.add(new SuggestionDropDownItem(R.drawable.ic_multiply_black_24dp, "times per day ...", "", TextEntityType.TIMES_PER_DAY));
        }
        return suggestions;
    }
}
