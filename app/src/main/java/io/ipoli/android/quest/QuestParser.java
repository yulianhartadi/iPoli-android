package io.ipoli.android.quest;

import android.support.v4.util.Pair;

import net.fortuna.ical4j.model.Recur;

import org.threeten.bp.LocalDate;

import java.util.Date;

import io.ipoli.android.app.parsers.DateTimeParser;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.EndDateMatcher;
import io.ipoli.android.quest.parsers.Match;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfMonthMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfWeekMatcher;
import io.ipoli.android.quest.parsers.RecurrenceEveryDayMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesAMonthMatcher;
import io.ipoli.android.quest.parsers.TimesAWeekMatcher;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class QuestParser {

    public class QuestParserResult {
        public String rawText;
        public String name;
        public int duration;
        public Integer startMinute;
        public LocalDate endDate;
        public int timesAWeek;
        public int timesAMonth;
        public Recur everyDayRecurrence;
        public Recur dayOfWeekRecurrence;
        public Recur dayOfMonthRecurrence;
    }

    private final EndDateMatcher endDateMatcher;
    private final StartTimeMatcher startTimeMatcher;
    private final DurationMatcher durationMatcher = new DurationMatcher();
    private final RecurrenceEveryDayMatcher everyDayMatcher = new RecurrenceEveryDayMatcher();
    private final RecurrenceDayOfWeekMatcher dayOfWeekMatcher = new RecurrenceDayOfWeekMatcher();
    private final RecurrenceDayOfMonthMatcher dayOfMonthMatcher = new RecurrenceDayOfMonthMatcher();
    private final TimesAWeekMatcher timesAWeekMatcher = new TimesAWeekMatcher();
    private final TimesAMonthMatcher timesAMonthMatcher = new TimesAMonthMatcher();

    public QuestParser(DateTimeParser timeParser) {
        this(timeParser, new Date());
    }

    public QuestParser(DateTimeParser timeParser, Date currentDate) {
        startTimeMatcher = new StartTimeMatcher(timeParser);
        endDateMatcher = new EndDateMatcher(timeParser, currentDate);
    }

    public Quest parseQuest(String text) {

        String rawText = text;

        Pair<Integer, String> durationPair = parseQuestPart(text, durationMatcher);
        int duration = durationPair.first;

        Pair<Integer, String> startTimePair = parseQuestPart(durationPair.second, startTimeMatcher);
        Integer startTime = startTimePair.first;

        Pair<LocalDate, String> dueDatePair = parseQuestPart(startTimePair.second, endDateMatcher);
        LocalDate dueDate = dueDatePair.first;
        text = dueDatePair.second;

        String name = text.trim();

        if (name.isEmpty()) {
            return null;
        }

        Quest q = new Quest(name);
        q.setRawText(rawText);
        q.setDuration(duration);
        q.setScheduledDate(dueDate);
        q.setEndDate(dueDate);
        q.setStartDate(dueDate);
        q.setStartMinute(startTime);
        return q;
    }

    private <T> Pair<T, String> parseQuestPart(String text, QuestTextMatcher<T> matcher) {
        Match match = matcher.match(text);
        String matchedText = match != null ? match.text : "";
        T parsedText = matcher.parse(text);
        text = text.replace(matchedText.trim(), "");
        return new Pair<>(parsedText, text);
    }
}
