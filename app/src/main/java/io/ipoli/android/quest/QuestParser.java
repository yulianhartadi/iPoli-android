package io.ipoli.android.quest;

import android.support.v4.util.Pair;

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.util.Date;

import io.ipoli.android.app.utils.DateUtils;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.EndDateMatcher;
import io.ipoli.android.quest.parsers.Match;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfMonthMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfWeekMatcher;
import io.ipoli.android.quest.parsers.RecurrenceEveryDayMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;

import static io.ipoli.android.app.utils.DateUtils.toStartOfDayUTC;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class QuestParser {

    public class QuestParserResult {
        public String rawText;
        public String name;
        public int duration;
        public int startMinute;
        public Date endDate;
        public int timesPerDay;
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
    private final TimesPerDayMatcher timesPerDayMatcher = new TimesPerDayMatcher();

    public QuestParser(PrettyTimeParser timeParser) {
        startTimeMatcher = new StartTimeMatcher(timeParser);
        endDateMatcher = new EndDateMatcher(timeParser);
    }

    public QuestParserResult parseText(String text) {
        QuestParserResult result = new QuestParserResult();
        result.rawText = text;

        Pair<Integer, String> durationPair = parseQuestPart(text, durationMatcher);
        result.duration = durationPair.first;

        Pair<Integer, String> startTimePair = parseQuestPart(durationPair.second, startTimeMatcher);
        result.startMinute = startTimePair.first;

        Pair<Date, String> dueDatePair = parseQuestPart(startTimePair.second, endDateMatcher);
        result.endDate = DateUtils.toStartOfDayUTC(new LocalDate(dueDatePair.first));

        Pair<Integer, String> timesPerDayPair = parseQuestPart(dueDatePair.second, timesPerDayMatcher);
        result.timesPerDay = Math.max(timesPerDayPair.first, 1);

        Pair<Recur, String> everyDayPair = parseQuestPart(timesPerDayPair.second, everyDayMatcher);
        result.everyDayRecurrence = everyDayPair.first;

        Pair<Recur, String> dayOfWeekPair = parseQuestPart(everyDayPair.second, dayOfWeekMatcher);
        result.dayOfWeekRecurrence = dayOfWeekPair.first;

        Pair<Recur, String> dayOfMonthPair = parseQuestPart(dayOfWeekPair.second, dayOfMonthMatcher);
        result.dayOfMonthRecurrence = dayOfMonthPair.first;

        text = dayOfMonthPair.second;
        result.name = text.trim();
        return result;
    }

    public Quest parse(String text) {

        String rawText = text;

        Pair<Integer, String> durationPair = parseQuestPart(text, durationMatcher);
        int duration = durationPair.first;

        Pair<Integer, String> startTimePair = parseQuestPart(durationPair.second, startTimeMatcher);
        int startTime = startTimePair.first;

        Pair<Date, String> dueDatePair = parseQuestPart(startTimePair.second, endDateMatcher);
        Date dueDate = dueDatePair.first;
        text = dueDatePair.second;

        String name = text.trim();

        if (name.isEmpty()) {
            return null;
        }

        Quest q = new Quest(name);
        q.setRawText(rawText);
        q.setDuration(duration);
        q.setEndDateFromLocal(dueDate);
        q.setStartMinute(startTime);
        return q;
    }

    public RepeatingQuest parseRepeatingQuest(String text) {

        String rawText = text;

        Pair<Integer, String> durationPair = parseQuestPart(text, durationMatcher);
        int duration = durationPair.first;

        Pair<Integer, String> startTimePair = parseQuestPart(durationPair.second, startTimeMatcher);
        int startMinute = startTimePair.first;

        Pair<Integer, String> timesPerDayPair = parseQuestPart(startTimePair.second, timesPerDayMatcher);
        int timesPerDay = timesPerDayPair.first;

        Pair<Recur, String> everyDayPair = parseQuestPart(timesPerDayPair.second, everyDayMatcher);
        Recur everyDayRecur = everyDayPair.first;

        Pair<Recur, String> dayOfWeekPair = parseQuestPart(everyDayPair.second, dayOfWeekMatcher);
        Recur dayOfWeekRecur = dayOfWeekPair.first;

        Pair<Recur, String> dayOfMonthPair = parseQuestPart(dayOfWeekPair.second, dayOfMonthMatcher);
        Recur dayOfMonthRecur = dayOfMonthPair.first;
        text = dayOfMonthPair.second;

        Date dueDate = null;
        if (everyDayRecur == null && dayOfWeekRecur == null && dayOfMonthRecur == null) {
            Pair<Date, String> dueDatePair = parseQuestPart(text, endDateMatcher);
            dueDate = dueDatePair.first;
            text = dueDatePair.second;
        }

        String name = text.trim();

        if (name.isEmpty()) {
            return null;
        }

        RepeatingQuest rq = new RepeatingQuest(rawText);
        rq.setName(name);
        rq.setDuration(duration);
        rq.setStartMinute(startMinute);
        Recurrence recurrence = new Recurrence(Math.max(1, timesPerDay));
        recurrence.setDtstart(toStartOfDayUTC(LocalDate.now()));
        if (everyDayRecur != null) {
            recurrence.setRrule(everyDayRecur.toString());
            recurrence.setType(Recurrence.RecurrenceType.WEEKLY);
        } else if (dayOfWeekRecur != null) {
            recurrence.setRrule(dayOfWeekRecur.toString());
            recurrence.setType(Recurrence.RecurrenceType.WEEKLY);
        } else if (dayOfMonthRecur != null) {
            recurrence.setRrule(dayOfMonthRecur.toString());
            recurrence.setType(Recurrence.RecurrenceType.MONTHLY);
        } else {
            recurrence.setRrule(null);
            if (dueDate != null) {
                recurrence.setDtstart(toStartOfDayUTC(new LocalDate(dueDate)));
                recurrence.setDtend(toStartOfDayUTC(new LocalDate(dueDate).plusDays(1)));
            } else {
                recurrence.setDtstart(null);
                recurrence.setDtend(null);
            }
        }

        rq.setRecurrence(recurrence);

        return rq;
    }

    public boolean isRepeatingQuest(String text) {
        for (QuestTextMatcher matcher : new QuestTextMatcher[]{everyDayMatcher, dayOfWeekMatcher, dayOfMonthMatcher, timesPerDayMatcher}) {
            if (matcher.match(text) != null) {
                return true;
            }
        }
        return false;
    }

    private <T> Pair<T, String> parseQuestPart(String text, QuestTextMatcher<T> matcher) {
        Match match = matcher.match(text);
        String matchedText = match != null ? match.text : "";
        T parsedText = matcher.parse(text);
        text = text.replace(matchedText.trim(), "");
        return new Pair<>(parsedText, text);
    }
}
