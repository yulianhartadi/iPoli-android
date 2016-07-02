package io.ipoli.android.quest.suggestions.providers;

import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class TimesPerDayTextSuggestionsProvider extends BaseSuggestionsProvider {

    @Override
    protected List<String> getSuggestions() {
        return Arrays.asList(new String[]{"2 times per day", "3 times per day", "4 times per day", "5 times per day", "6 times per day", "7 times per day"});
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
