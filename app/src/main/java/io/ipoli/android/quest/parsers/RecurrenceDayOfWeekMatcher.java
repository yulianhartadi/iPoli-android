package io.ipoli.android.quest.parsers;

import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.DayOfWeekSuggestionsProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class RecurrenceDayOfWeekMatcher extends RecurrenceEveryDayMatcher {
    public static final String[] WEEKDAY_NAMES = new String[]{"mon", "tue", "wed", "thu", "fri", "sat", "sun"};

    private static final String WEEKDAY_PATTERN = "(?:^|\\s)every(((\\,\\s?|\\s|\\sand\\s|\\s\\&\\s)?(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Mon|Tue|Wed|Thur|Fri|Sat|Sun))+)(?:$|\\s)";

    private Pattern[] patterns = {
            Pattern.compile(WEEKDAY_PATTERN, Pattern.CASE_INSENSITIVE)
    };

    public RecurrenceDayOfWeekMatcher() {
        super(new DayOfWeekSuggestionsProvider());
    }

    @Override
    public Recur parse(String text) {
        for (Pattern p : getPatterns()) {
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                Recur recur = new Recur(Recur.WEEKLY, null);
                String weekdays = matcher.group(1).toLowerCase();
                for (String weekdayName : WEEKDAY_NAMES) {
                    if (weekdays.contains(weekdayName)) {
                        recur.getDayList().add(new WeekDay(weekdayName.substring(0, 2).toUpperCase()));
                    }
                }
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
        return TextEntityType.RECURRENT_DAY_OF_WEEK;
    }
}
