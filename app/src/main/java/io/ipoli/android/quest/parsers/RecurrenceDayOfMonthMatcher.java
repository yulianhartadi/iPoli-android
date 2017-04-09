package io.ipoli.android.quest.parsers;

import net.fortuna.ical4j.model.Recur;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.DayOfMonthSuggestionsProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class RecurrenceDayOfMonthMatcher extends RecurrenceEveryDayMatcher {
    private static final String ON_EVERY_MONTH_PATTERN = "(?:^|\\s)on\\s(\\d{1,2})(st|nd|th|rd)?\\s(every|each)\\smonth(?:$|\\s)";
    private static final String EVERY_OF_THE_MONTH_PATTERN = "(?:^|\\s)(every|each)\\s(\\d{1,2})(st|th|rd|nd)?\\sof\\sthe\\smonth(?:$|\\s)";

    private Pattern[] patterns = {
            Pattern.compile(ON_EVERY_MONTH_PATTERN, Pattern.CASE_INSENSITIVE),
            Pattern.compile(EVERY_OF_THE_MONTH_PATTERN, Pattern.CASE_INSENSITIVE),
    };

    public RecurrenceDayOfMonthMatcher() {
        super(new DayOfMonthSuggestionsProvider());
    }

    @Override
    public Recur parse(String text) {
        for (Pattern p : getPatterns()) {
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                Recur recur = new Recur(Recur.MONTHLY, null);
                String matchedDayText = p.pattern().equals(ON_EVERY_MONTH_PATTERN) ? matcher.group(1) : matcher.group(2);
                recur.getMonthDayList().add(Integer.valueOf(matchedDayText));
                return recur;
            }
        }
        return null;
    }

    @Override
    protected Pattern[] getPatterns() {
        return patterns;
    }

    @Override
    public TextEntityType getTextEntityType() {
        return TextEntityType.RECURRENT_DAY_OF_MONTH;
    }
}
