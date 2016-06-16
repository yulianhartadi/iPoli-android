package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.quest.ui.formatters.DurationFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DurationSuggestionsProvider extends BaseSuggestionsProvider {

    @Override
    protected List<String> getSuggestions() {
        List<String> durations = new ArrayList<>();
        for(int d : Constants.DURATIONS) {
            durations.add(DurationFormatter.formatReadableShort(d));
        }
        return durations;
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
