package io.ipoli.android.quest.suggestions.providers;

import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Arrays;

import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class DurationSuggestionsProvider extends BaseSuggestionsProvider {

    @Override
    protected List<String> getSuggestions() {
        return Arrays.asList(new String[]{"5 min", "10 min", "15 min", "30 min", "1 hour", "1 h and 30 m", "2 hours", "3 hours", "4 hours"});
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
