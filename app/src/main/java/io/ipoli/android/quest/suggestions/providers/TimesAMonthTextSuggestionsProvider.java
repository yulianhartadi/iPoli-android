package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.quest.ui.formatters.FlexibleTimesFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class TimesAMonthTextSuggestionsProvider extends BaseSuggestionsProvider {

    @Override
    protected List<String> getSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for(int i = Constants.MIN_FLEXIBLE_TIMES_A_MONTH_COUNT; i <= Constants.MAX_FLEXIBLE_TIMES_A_MONTH_COUNT; i++) {
            suggestions.add(FlexibleTimesFormatter.formatReadable(i) + " a month");
        }
        return suggestions;
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_calendar_multiple_black_24dp;
    }

    @Override
    protected String getMatchingStartWord() {
        return "";
    }
}
