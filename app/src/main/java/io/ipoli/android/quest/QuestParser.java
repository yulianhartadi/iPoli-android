package io.ipoli.android.quest;

import android.support.v4.util.Pair;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.shade.net.fortuna.ical4j.model.Recur;

import java.util.Date;

import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.Recurrence;
import io.ipoli.android.quest.data.RepeatingQuest;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.Match;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfMonthMatcher;
import io.ipoli.android.quest.parsers.RecurrenceDayOfWeekMatcher;
import io.ipoli.android.quest.parsers.RecurrenceEveryDayMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class QuestParser {

    private final DueDateMatcher dueDateMatcher;
    private final StartTimeMatcher startTimeMatcher;
    private final DurationMatcher durationMatcher = new DurationMatcher();
    private final RecurrenceEveryDayMatcher everyDayMatcher = new RecurrenceEveryDayMatcher();
    private final RecurrenceDayOfWeekMatcher dayOfWeekMatcher = new RecurrenceDayOfWeekMatcher();
    private final RecurrenceDayOfMonthMatcher dayOfMonthMatcher = new RecurrenceDayOfMonthMatcher();
    private final TimesPerDayMatcher timesPerDayMatcher = new TimesPerDayMatcher();

    public QuestParser(PrettyTimeParser timeParser) {
        startTimeMatcher = new StartTimeMatcher(timeParser);
        dueDateMatcher = new DueDateMatcher(timeParser);
    }

    public Quest parse(String text) {

        String rawText = text;

        Pair<Integer, String> durationPair = parseQuestPart(text, durationMatcher);
        int duration = durationPair.first;

        Pair<Integer, String> startTimePair = parseQuestPart(durationPair.second, startTimeMatcher);
        int startTime = startTimePair.first;

        Pair<Date, String> dueDatePair = parseQuestPart(startTimePair.second, dueDateMatcher);
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

        Pair<Recur, String> everyDayRes = parseQuestPart(timesPerDayPair.second, everyDayMatcher);
        Recur everyDayRecur = everyDayRes.first;

        Pair<Recur, String> dayOfWeekPair = parseQuestPart(everyDayRes.second, dayOfWeekMatcher);
        Recur dayOfWeekRecur = dayOfWeekPair.first;

        Pair<Recur, String> dayOfMonthPair = parseQuestPart(dayOfWeekPair.second, dayOfMonthMatcher);
        Recur dayOfMonthRecur = dayOfMonthPair.first;
        text = dayOfMonthPair.second;

        String name = text.trim();

        if (name.isEmpty()) {
            return null;
        }

        RepeatingQuest rq = new RepeatingQuest(rawText);
        rq.setName(name);
        rq.setDuration(duration);
        rq.setStartMinute(startMinute);
        Recurrence recurrence = new Recurrence(timesPerDay);
        if (everyDayRecur != null) {
            recurrence.setRrule(everyDayRecur.toString(), Recurrence.RecurrenceType.WEEKLY);
        } else if (dayOfWeekRecur != null) {
            recurrence.setRrule(dayOfWeekRecur.toString(), Recurrence.RecurrenceType.WEEKLY);
        } else if (dayOfMonthRecur != null) {
            recurrence.setRrule(dayOfMonthRecur.toString(), Recurrence.RecurrenceType.MONTHLY);
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
