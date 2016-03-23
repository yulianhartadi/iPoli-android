package io.ipoli.android.quest.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/23/16.
 */
public class RecurrenceMatcher implements QuestTextMatcher<String> {
    private static final String EVERY_DAY_PATTERN = "\\severy\\sday";
    private static final String WEEKDAY_PATTERN = "\\severy((\\,\\s?|\\s|\\sand\\s|\\s\\&\\s)?(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Mon|Tue|Wed|Thur|Fri|Sat|Sun))+";
    private static final String ON_EVERY_MONTH_PATTERN = "\\son\\s(\\d{1,2})(st|nd|th|rd)?\\s(every|each)\\smonth";
    private static final String EVERY_OF_THE_MONTH_PATTERN = "\\s(every|each)\\s(\\d{1,2})(st|th|rd|nd)?\\sof\\sthe\\smonth";

    private Pattern[] patterns = {
            Pattern.compile(EVERY_DAY_PATTERN, Pattern.CASE_INSENSITIVE),
            Pattern.compile(WEEKDAY_PATTERN, Pattern.CASE_INSENSITIVE),
            Pattern.compile(ON_EVERY_MONTH_PATTERN, Pattern.CASE_INSENSITIVE),
            Pattern.compile(EVERY_OF_THE_MONTH_PATTERN, Pattern.CASE_INSENSITIVE),
    };

    @Override
    public String match(String text) {
        for (Pattern p : patterns) {
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return "";
    }

    @Override
    public String parse(String text) {
        for (Pattern p : patterns) {
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return null;
    }
}
