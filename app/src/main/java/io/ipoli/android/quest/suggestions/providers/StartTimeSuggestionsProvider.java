package io.ipoli.android.quest.suggestions.providers;

import android.support.annotation.NonNull;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        createSuggestions();
        createSuggestionItems();
    }

    @Override
    protected void createSuggestionItems() {
        for(Map.Entry<String, String> entry : suggestionToVisibleText.entrySet()) {
            defaultSuggestionItems.add(new SuggestionDropDownItem(getIcon(), entry.getValue(), getMatchingStartWord() + entry.getKey()));
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
    private void createSuggestions() {
        int interval = 15;

        LocalTime now = LocalTime.now();
        int nextClosestRoundMinute = ((now.getMinute() / interval) + 1) * interval;
        now = now.plusMinutes(nextClosestRoundMinute - now.getMinute());

        int count = 24 * (60 / interval);
        for (int i = 1; i <= count; i++) {
            Instant timeInstant = now.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
            String text = StartTimeFormatter.formatShort(new Date(timeInstant.toEpochMilli()), use24HourFormat);
            suggestionToVisibleText.put(text, text);
            now = now.plusMinutes(interval);
        }
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
