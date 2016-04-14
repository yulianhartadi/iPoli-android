package io.ipoli.android.quest.suggestions.providers;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.suggestions.TextEntityType;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/6/16.
 */
public class DayOfWeekSuggestionsProvider implements SuggestionsProvider {
    private static final int ICON = R.drawable.ic_repeat_black_24dp;

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        text = text.toLowerCase();
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        if(!text.contains("mon")) {
            suggestions.add(new SuggestionDropDownItem(ICON, "Monday", "Mon", TextEntityType.RECURRENT_DAY_OF_WEEK, false, false));
        }
        if(!text.contains("tue")) {
            suggestions.add(new SuggestionDropDownItem(ICON, "Tuesday", "Tue", TextEntityType.RECURRENT_DAY_OF_WEEK, false, false));
        }
        if(!text.contains("wed")) {
            suggestions.add(new SuggestionDropDownItem(ICON, "Wednesday", "Wed", TextEntityType.RECURRENT_DAY_OF_WEEK, false, false));
        }
        if(!text.contains("thur")) {
            suggestions.add(new SuggestionDropDownItem(ICON, "Thursday", "Thur", TextEntityType.RECURRENT_DAY_OF_WEEK, false, false));
        }
        if(!text.contains("fri")) {
            suggestions.add(new SuggestionDropDownItem(ICON, "Friday", "Fri", TextEntityType.RECURRENT_DAY_OF_WEEK, false, false));
        }
        if(!text.contains("sat")) {
            suggestions.add(new SuggestionDropDownItem(ICON, "Saturday", "Sat", TextEntityType.RECURRENT_DAY_OF_WEEK, false, false));
        }
        if(!text.contains("sun")) {
            suggestions.add(new SuggestionDropDownItem(ICON, "Sunday", "Sun", TextEntityType.RECURRENT_DAY_OF_WEEK, false, false));
        }
        return suggestions;
    }
}
