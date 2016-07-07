package io.ipoli.android.quest.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.quest.suggestions.MatcherType;
import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.TimesAMonthTextSuggestionsProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class TimesAMonthMatcher extends BaseMatcher<Integer> {
    private static final String PATTERN = "(?:^|\\s)([1-9]|1[0-5])\\stime(s)?(?:\\sa\\smonth)+(?:$|\\s)";
    Pattern pattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    public TimesAMonthMatcher() {
        super(new TimesAMonthTextSuggestionsProvider());
    }

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
            if(tpd > 0) {
                return tpd;
            }
        }
        return -1;
    }

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.DATE;
    }

    @Override
    public TextEntityType getTextEntityType() {
        return TextEntityType.TIMES_A_MONTH;
    }

    @Override
    public boolean partiallyMatches(String text) {
        Matcher m = pattern.matcher(text);
        m.matches();
        return m.hitEnd();
    }
}
