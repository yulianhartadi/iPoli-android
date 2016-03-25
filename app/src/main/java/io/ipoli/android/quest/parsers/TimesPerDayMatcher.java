package io.ipoli.android.quest.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Polina Zhelyazkova <poly_vjk@abv.bg>
 * on 3/23/16.
 */
public class TimesPerDayMatcher implements QuestTextMatcher<Integer> {
    private static final String PATTERN = "\\s(\\d+)\\stimes(?:\\sper\\sday)?";
    Pattern pattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    @Override
    public String match(String text) {
        Matcher m = pattern.matcher(text);
        if (m.find()) {
            return m.group();
        }
        return "";
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
}
