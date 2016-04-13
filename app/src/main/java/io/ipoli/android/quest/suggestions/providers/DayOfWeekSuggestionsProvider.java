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

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        int icon = R.drawable.ic_repeat_black_24dp;
        List<SuggestionDropDownItem> suggestions = new ArrayList<>();
        if(!text.contains("Mon")) {
            suggestions.add(new SuggestionDropDownItem(icon, "Monday", "Mon", TextEntityType.RECURRENT_DAY_OF_WEEK, false));
        }
        if(!text.contains("Tue")) {
            suggestions.add(new SuggestionDropDownItem(icon, "Tuesday", "Tue", TextEntityType.RECURRENT_DAY_OF_WEEK, false));
        }
        if(!text.contains("Wed")) {
            suggestions.add(new SuggestionDropDownItem(icon, "Wednesday", "Wed", TextEntityType.RECURRENT_DAY_OF_WEEK, false));
        }
        if(!text.contains("Thur")) {
            suggestions.add(new SuggestionDropDownItem(icon, "Thursday", "Thur", TextEntityType.RECURRENT_DAY_OF_WEEK, false));
        }
        if(!text.contains("Fri")) {
            suggestions.add(new SuggestionDropDownItem(icon, "Friday", "Fri", TextEntityType.RECURRENT_DAY_OF_WEEK, false));
        }
        if(!text.contains("Sat")) {
            suggestions.add(new SuggestionDropDownItem(icon, "Saturday", "Sat", TextEntityType.RECURRENT_DAY_OF_WEEK, false));
        }
        if(!text.contains("Sun")) {
            suggestions.add(new SuggestionDropDownItem(icon, "Sunday", "Sun", TextEntityType.RECURRENT_DAY_OF_WEEK, false));
        }
        return suggestions;
    }
}
