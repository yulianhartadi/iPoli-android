package io.ipoli.android.quest;

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

        Match durationMatch = durationMatcher.match(text);
        String matchedDurationText = durationMatch != null ? durationMatch.text : "";
        int duration = durationMatcher.parse(text);
        text = text.replace(matchedDurationText.trim(), "");

        Match startTimeMatch = startTimeMatcher.match(text);
        String matchedStartTimeText = startTimeMatch != null ? startTimeMatch.text : "";
        int startTime = startTimeMatcher.parse(text);
        text = text.replace(matchedStartTimeText.trim(), "");

        Match dueDateMatch = dueDateMatcher.match(text);
        String matchedDueDateText = dueDateMatch != null ? dueDateMatch.text : "";
        Date dueDate = dueDateMatcher.parse(text);
        text = text.replace(matchedDueDateText.trim(), "");

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

        Match durationMatch = durationMatcher.match(text);
        String matchedDurationText = durationMatch != null ? durationMatch.text : "";
        int duration = durationMatcher.parse(text);
        text = text.replace(matchedDurationText.trim(), "");

        Match startTimeMatch = startTimeMatcher.match(text);
        String matchedStartTimeText = startTimeMatch != null ? startTimeMatch.text : "";
        int startMinute = startTimeMatcher.parse(text);
        text = text.replace(matchedStartTimeText.trim(), "");

        Match timesPerDayMatch = timesPerDayMatcher.match(text);
        String timesPerDayText = timesPerDayMatch != null ? timesPerDayMatch.text : "";
        int timesPerDay = Math.max(1, timesPerDayMatcher.parse(text));
        text = text.replace(timesPerDayText.trim(), "");

        Match everyDayMatch = everyDayMatcher.match(text);
        String everyDayText = everyDayMatch != null ? everyDayMatch.text : "";
        Recur everyDayRecur = everyDayMatcher.parse(text);
        text = text.replace(everyDayText.trim(), "");

        Match dayOfWeekMatch = dayOfWeekMatcher.match(text);
        String dayOfWeekText = dayOfWeekMatch != null ? dayOfWeekMatch.text : "";
        Recur dayOfWeekRecur = dayOfWeekMatcher.parse(text);
        text = text.replace(dayOfWeekText.trim(), "");

        Match dayOfMonthMatch = dayOfMonthMatcher.match(text);
        String dayOfMonthText = dayOfMonthMatch != null ? dayOfMonthMatch.text : "";
        Recur dayOfMonthRecur = dayOfMonthMatcher.parse(text);
        text = text.replace(dayOfMonthText.trim(), "");

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
}
