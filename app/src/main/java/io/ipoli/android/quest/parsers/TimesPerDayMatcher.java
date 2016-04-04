package io.ipoli.android.quest.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class TimesPerDayMatcher extends BaseMatcher<Integer> {
    private static final String PATTERN = "(?:^|\\s)(\\d+)\\stimes(?:\\sper\\sday)?(?:$|\\s)";
    Pattern pattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    @Override
    public Match match(String text) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            return new Match(m.group(), m.start(), m.end() - 1);
        }
        return null;
    }

    @Override
    public Integer parse(String text) {
        Matcher tpdm = pattern.matcher(text);
        if (tpdm.find()) {
            int tpd = Integer.valueOf(tpdm.group(1));
            if(tpd > 1) {
                return tpd;
            }
        }
        return -1;
    }

    @Override
    public boolean partiallyMatches(String text) {
        Matcher m = pattern.matcher(text);
        m.matches();
        return m.hitEnd();
    }
}
