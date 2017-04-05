package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.DurationFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DurationSuggestionsProvider extends BaseSuggestionsProvider {

    public DurationSuggestionsProvider() {
        createSuggestions();
        createSuggestionItems();
    }

    private void createSuggestions() {
        for (int d : Constants.DURATIONS) {
            String visibleText = DurationFormatter.formatReadableShort(d);
            String text = d == Constants.QUEST_MIN_DURATION ? DurationFormatter.formatShort(d) : visibleText;
            suggestionToVisibleText.put(text, visibleText);
        }
    }

    @Override
    protected List<String> getSuggestions() {
        return new ArrayList<>();
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_timer_black_18dp;
    }

    @Override
    protected String getMatchingStartWord() {
        return "for ";
    }
}
