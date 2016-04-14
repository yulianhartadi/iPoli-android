package io.ipoli.android.quest.suggestions.providers;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;
import io.ipoli.android.quest.ui.formatters.StartTimeFormatter;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class StartTimeSuggestionsProvider implements SuggestionsProvider {
    private static final int icon = R.drawable.ic_clock_black_24dp;
    private List<String> allTimes = new ArrayList<>();

    public StartTimeSuggestionsProvider() {
        int interval = 15;

        LocalTime now = LocalTime.now();
        int nextClosestRoundMinute = ((now.getMinuteOfHour() / interval) + 1) * interval;
        now = now.plusMinutes(nextClosestRoundMinute - now.getMinuteOfHour());

        int count = 24 * (60 / interval);
        for (int i = 1; i <= count; i++) {
            allTimes.add(StartTimeFormatter.format(now.toDateTimeToday().toDate()));
            now = now.plusMinutes(interval);
        }
    }

    @Override
    public List<SuggestionDropDownItem> filter(String text) {
        List<SuggestionDropDownItem> suggestionItems = new ArrayList<>();
        if ("at ".contains(text.toLowerCase())) {
            for (String s : allTimes) {
                suggestionItems.add(new SuggestionDropDownItem(icon, s, "at " + s));
            }
            return suggestionItems;
        }

        if (text.toLowerCase().startsWith("at ")) {
            text = text.replaceFirst("at\\s", "");
        }

        for(String s: allTimes) {
            if(s.startsWith(text)) {
                suggestionItems.add(new SuggestionDropDownItem(icon, s, "at " + s));
            }
        }

        return suggestionItems;
    }
}
