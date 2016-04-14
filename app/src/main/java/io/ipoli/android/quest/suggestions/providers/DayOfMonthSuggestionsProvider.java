package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/6/16.
 */
public class DayOfMonthSuggestionsProvider extends BaseSuggestionsProvider {
    @Override
    protected List<String> getSuggestions() {
        List<String> suggestions = new ArrayList<>();
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        for (int i = today; i <= 31; i++) {
            suggestions.add(generateSuggestionText(i));
        }

        for (int i = 1; i < today; i++) {
            suggestions.add(generateSuggestionText(i));
        }
        return suggestions;
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_repeat_black_24dp;
    }

    @Override
    protected String getMatchingStartWord() {
        return "every ";
    }

    private String generateSuggestionText(int day) {
        return day + getDayOfMonthSuffix(day) + " of the month";
    }

    private String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}