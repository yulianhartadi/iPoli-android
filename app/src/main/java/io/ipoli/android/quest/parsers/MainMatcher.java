package io.ipoli.android.quest.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/16.
 */
public class MainMatcher implements QuestTextMatcher<String> {
    private static final String PATTERN = "(?:^|\\s)(on|at|for|every|times\\sper\\sday)\\s";
    private Pattern pattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    @Override
    public String match(String text) {
        Matcher m = pattern.matcher(text);
        if(m.find()) {
            return m.group(1);
        }
        return "";
    }

    @Override
    public String parse(String text) {
        Matcher m = pattern.matcher(text);
        if(m.find()) {
            return m.group();
        }
        return null;
    }

    @Override
    public boolean partiallyMatches(String text) {
        Matcher m = pattern.matcher(text);
        m.matches();
        return m.hitEnd();
    }
}
