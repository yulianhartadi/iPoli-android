package io.ipoli.android.quest.parsers;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class StartTimeMatcher implements QuestTextMatcher<Date> {

    private static final String PATTERN = " at (\\d{1,2}[:|\\.]?(\\d{2})?\\s?(am|pm)?)";
    private final PrettyTimeParser parser;
    private Pattern pattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    public StartTimeMatcher(PrettyTimeParser parser) {
        this.parser = parser;
    }

    @Override
    public String match(String text) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return "";
    }

    @Override
    public Date parse(String text) {
        Matcher stm = pattern.matcher(text);
        if (stm.find()) {
            List<Date> dates = parser.parse(stm.group());
            if (!dates.isEmpty()) {
                return dates.get(0);
            }
        }
        return null;
    }
}
