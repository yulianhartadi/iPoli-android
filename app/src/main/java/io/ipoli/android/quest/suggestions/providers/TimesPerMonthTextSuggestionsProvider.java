package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class TimesPerMonthTextSuggestionsProvider extends BaseSuggestionsProvider {

    @Override
    protected List<String> getSuggestions() {
        List<String> suggestions = new ArrayList<>();
        for(int i = 2; i<=15; i++) {
            suggestions.add(i + " times per month");
        }
        return suggestions;
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_multiply_black_24dp_transparent;
    }

    @Override
    protected String getMatchingStartWord() {
        return "";
    }
}
