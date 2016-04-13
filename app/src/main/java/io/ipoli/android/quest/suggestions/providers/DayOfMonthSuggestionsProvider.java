package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/6/16.
 */
public class DayOfMonthSuggestionsProvider implements SuggestionsProvider {
    private static final int icon = R.drawable.ic_repeat_black_24dp;
    private List<SuggestionDropDownItem> suggestionItems = new ArrayList<>();

    public DayOfMonthSuggestionsProvider() {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        for (int i = today; i <= 31; i++) {
            suggestionItems.add(generateSuggestionItem(i));
        }

        for (int i = 1; i < today; i++) {
            suggestionItems.add(generateSuggestionItem(i));
        }
    }

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        if(text.toLowerCase().startsWith("every ")) {
            text = text.replaceFirst("every\\s", "");
        }
        List<SuggestionDropDownItem> items = new ArrayList<>();
        for (SuggestionDropDownItem i : suggestionItems) {
            if (i.visibleText.startsWith(text)) {
                items.add(i);
            }
        }
        return items;
    }

    private SuggestionDropDownItem generateSuggestionItem(int day) {
        String text = generateSuggestionText(day);
        return new SuggestionDropDownItem(icon, text, "every " + text);
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