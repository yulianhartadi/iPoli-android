package io.ipoli.android.quest.suggestions.providers;

import android.support.annotation.NonNull;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

import io.ipoli.android.R;
import io.ipoli.android.app.ui.formatters.StartTimeFormatter;
import io.ipoli.android.quest.suggestions.SuggestionDropDownItem;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/27/16.
 */
public class StartTimeSuggestionsProvider extends BaseSuggestionsProvider {

    private final boolean use24HourFormat;

    public StartTimeSuggestionsProvider(boolean use24HourFormat) {
        this.use24HourFormat = use24HourFormat;
        for(String s : createSuggestions()) {
            defaultSuggestionItems.add(new SuggestionDropDownItem(getIcon(), s, getMatchingStartWord() + s));
        }
    }

    public StartTimeSuggestionsProvider() {
        this(true);
    }

    @Override
    protected List<String> getSuggestions() {
        return new ArrayList<>();
    }

    @NonNull
    private List<String> createSuggestions() {
        List<String> suggestions = new ArrayList<>();
        int interval = 15;

        LocalTime now = LocalTime.now();
        int nextClosestRoundMinute = ((now.getMinuteOfHour() / interval) + 1) * interval;
        now = now.plusMinutes(nextClosestRoundMinute - now.getMinuteOfHour());

        int count = 24 * (60 / interval);
        for (int i = 1; i <= count; i++) {
            suggestions.add(StartTimeFormatter.formatShort(now.toDateTimeToday().toDate(), use24HourFormat));
            now = now.plusMinutes(interval);
        }
        return suggestions;
    }

    @Override
    protected int getIcon() {
        return R.drawable.ic_clock_black_24dp;
    }

    @Override
    protected String getMatchingStartWord() {
        return "at ";
    }
}
