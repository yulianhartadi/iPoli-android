package io.ipoli.android.quest;

import android.support.v4.util.Pair;

import org.joda.time.LocalDate;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.util.Date;

import io.ipoli.android.Constants;
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
import io.ipoli.android.quest.parsers.TimesADayMatcher;
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
        public int startMinute;
        public Date endDate;
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

    public QuestParser(PrettyTimeParser timeParser) {
        startTimeMatcher = new StartTimeMatcher(timeParser);
        endDateMatcher = new EndDateMatcher(timeParser);
    }

    public QuestParserResult parse(String text) {
        QuestParserResult result = new QuestParserResult();
        result.rawText = text;

        Pair<Integer, String> durationPair = parseQuestPart(text, durationMatcher);
        result.duration = durationPair.first;

        Pair<Integer, String> startTimePair = parseQuestPart(durationPair.second, startTimeMatcher);
        result.startMinute = startTimePair.first;

        Pair<Date, String> dueDatePair = parseQuestPart(startTimePair.second, endDateMatcher);
        if (dueDatePair.first != null) {
            result.endDate = DateUtils.toStartOfDayUTC(new LocalDate(dueDatePair.first));
        }

        Pair<Integer, String> timesAWeekPair = parseQuestPart(dueDatePair.second, timesAWeekMatcher);
        result.timesAWeek = Math.max(timesAWeekPair.first, 0);

        Pair<Integer, String> timesAMonthPair = parseQuestPart(timesAWeekPair.second, timesAMonthMatcher);
        result.timesAMonth = Math.max(timesAMonthPair.first, 0);

        Pair<Recur, String> everyDayPair = parseQuestPart(timesAMonthPair.second, everyDayMatcher);
        result.everyDayRecurrence = everyDayPair.first;

        Pair<Recur, String> dayOfWeekPair = parseQuestPart(everyDayPair.second, dayOfWeekMatcher);
        result.dayOfWeekRecurrence = dayOfWeekPair.first;

        Pair<Recur, String> dayOfMonthPair = parseQuestPart(dayOfWeekPair.second, dayOfMonthMatcher);
        result.dayOfMonthRecurrence = dayOfMonthPair.first;

        text = dayOfMonthPair.second;
        result.name = text.trim();
        return result;
    }

    public Quest parseQuest(String text) {

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
        q.setStartDateFromLocal(dueDate);
        q.setStartMinute(startTime);
        return q;
    }

    public RepeatingQuest parseRepeatingQuest(String text) {

        String rawText = text;

        Pair<Integer, String> durationPair = parseQuestPart(text, durationMatcher);
        int duration = Math.max(durationPair.first, Constants.QUEST_MIN_DURATION);

        Pair<Integer, String> startTimePair = parseQuestPart(durationPair.second, startTimeMatcher);
        int startMinute = startTimePair.first;

        Pair<Integer, String> timesADayPair = parseQuestPart(startTimePair.second, new TimesADayMatcher());
        int timesADay = Math.max(timesADayPair.first, 0);

        Pair<Integer, String> timesAWeekPair = parseQuestPart(timesADayPair.second, timesAWeekMatcher);
        int timesAWeek = Math.max(timesAWeekPair.first, 0);

        Pair<Integer, String> timesAMonthPair = parseQuestPart(timesAWeekPair.second, timesAMonthMatcher);
        int timesAMonth = Math.max(timesAMonthPair.first, 0);

        Pair<Recur, String> everyDayPair = parseQuestPart(timesAMonthPair.second, everyDayMatcher);
        Recur everyDayRecur = everyDayPair.first;

        Pair<Recur, String> dayOfWeekPair = parseQuestPart(everyDayPair.second, dayOfWeekMatcher);
        Recur dayOfWeekRecur = dayOfWeekPair.first;

        Pair<Recur, String> dayOfMonthPair = parseQuestPart(dayOfWeekPair.second, dayOfMonthMatcher);
        Recur dayOfMonthRecur = dayOfMonthPair.first;
        text = dayOfMonthPair.second;

        if (everyDayRecur == null && dayOfWeekRecur == null && dayOfMonthRecur == null) {
            Pair<Date, String> dueDatePair = parseQuestPart(text, endDateMatcher);
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
        Recurrence recurrence = Recurrence.create();
        if (everyDayRecur != null) {
            recurrence.setRrule(everyDayRecur.toString());
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
        } else if (dayOfWeekRecur != null) {
            recurrence.setRrule(dayOfWeekRecur.toString());
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
        } else if (dayOfMonthRecur != null) {
            recurrence.setRrule(dayOfMonthRecur.toString());
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.MONTHLY);
        } else if (timesAWeek > 0) {
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.WEEKLY);
            recurrence.setFlexibleCount(timesAWeek);
            Recur recur = new Recur(Recur.WEEKLY, null);
            recurrence.setRrule(recur.toString());
        } else if (timesAMonth > 0) {
            recurrence.setRecurrenceType(Recurrence.RecurrenceType.MONTHLY);
            recurrence.setFlexibleCount(timesAMonth);
            Recur recur = new Recur(Recur.MONTHLY, null);
            recurrence.setRrule(recur.toString());
        } else {
            recurrence.setRrule(Recurrence.RRULE_EVERY_DAY);
        }

        rq.setTimesADay(Math.max(1, timesADay));
        rq.setRecurrence(recurrence);

        return rq;
    }

    public boolean isRepeatingQuest(String text) {
        for (QuestTextMatcher matcher : new QuestTextMatcher[]{everyDayMatcher, dayOfWeekMatcher, dayOfMonthMatcher, timesAWeekMatcher, timesAMonthMatcher}) {
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
