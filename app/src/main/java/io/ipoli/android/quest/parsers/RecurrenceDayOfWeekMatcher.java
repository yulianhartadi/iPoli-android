package io.ipoli.android.quest.parsers;

import java.util.regex.Pattern;

import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.DayOfWeekSuggestionsProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class RecurrenceDayOfWeekMatcher extends RecurrenceEveryDayMatcher {
    private static final String WEEKDAY_PATTERN = "(?:^|\\s)every((\\,\\s?|\\s|\\sand\\s|\\s\\&\\s)?(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Mon|Tue|Wed|Thur|Fri|Sat|Sun))+(?:$|\\s)";

    private Pattern[] patterns = {
            Pattern.compile(WEEKDAY_PATTERN, Pattern.CASE_INSENSITIVE)
    };

    public RecurrenceDayOfWeekMatcher() {
        super(new DayOfWeekSuggestionsProvider());
    }

    @Override
    protected Pattern[] getPatterns() {
        return patterns;
    }

    @Override
    public TextEntityType getTextEntityType() {
        return TextEntityType.RECURRENT_DAY_OF_WEEK;
    }
}
