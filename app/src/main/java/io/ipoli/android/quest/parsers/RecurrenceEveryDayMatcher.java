package io.ipoli.android.quest.parsers;


import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.WeekDay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.ipoli.android.quest.suggestions.MatcherType;
import io.ipoli.android.quest.suggestions.TextEntityType;
import io.ipoli.android.quest.suggestions.providers.RecurrenceSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.SuggestionsProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/23/16.
 */
public class RecurrenceEveryDayMatcher extends BaseMatcher<Recur> {
    private static final String EVERY_DAY_PATTERN = "(?:^|\\s)every\\sday(?:$|\\s)";

    private Pattern[] patterns = {
            Pattern.compile(EVERY_DAY_PATTERN, Pattern.CASE_INSENSITIVE),
    };

    public RecurrenceEveryDayMatcher() {
        this(new RecurrenceSuggestionsProvider());
    }

    protected RecurrenceEveryDayMatcher(SuggestionsProvider suggestionsProvider) {
        super(suggestionsProvider);
    }

    @Override
    public Match match(String text) {
        for (Pattern p : getPatterns()) {
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                return new Match(matcher.group(), matcher.start(), matcher.end() - 1);
            }
        }
        return null;
    }

    @Override
    public Recur parse(String text) {
        for (Pattern p : getPatterns()) {
            Matcher matcher = p.matcher(text);
            if (matcher.find()) {
                Recur recur = new Recur(Recur.WEEKLY, null);
                recur.getDayList().add(WeekDay.MO);
                recur.getDayList().add(WeekDay.TU);
                recur.getDayList().add(WeekDay.WE);
                recur.getDayList().add(WeekDay.TH);
                recur.getDayList().add(WeekDay.FR);
                recur.getDayList().add(WeekDay.SA);
                recur.getDayList().add(WeekDay.SU);
                return recur;
            }
        }
        return null;
    }

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.DATE;
    }

    @Override
    public TextEntityType getTextEntityType() {
        return TextEntityType.RECURRENT;
    }

    @Override
    public boolean partiallyMatches(String text) {
        for (Pattern p : getPatterns()) {
            Matcher matcher = p.matcher(text);
            matcher.matches();
            if (matcher.hitEnd()) {
                return true;
            }
        }
        return false;
    }

    protected Pattern[] getPatterns() {
        return patterns;
    }
}
