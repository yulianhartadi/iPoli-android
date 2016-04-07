package io.ipoli.android.quest;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Date;

import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.data.RecurrentQuest;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.Match;
import io.ipoli.android.quest.parsers.RecurrenceMatcher;
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
    private final RecurrenceMatcher recurrenceMatcher = new RecurrenceMatcher();
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
        q.setEndDate(dueDate);
        q.setStartMinute(startTime);
        return q;
    }

    public RecurrentQuest parseRecurrent(String text) {

        String rawText = text;

        Match durationMatch = durationMatcher.match(text);
        String matchedDurationText = durationMatch != null ? durationMatch.text : "";
        text = text.replace(matchedDurationText.trim(), "");

        Match startTimeMatch = startTimeMatcher.match(text);
        String matchedStartTimeText = startTimeMatch != null ? startTimeMatch.text : "";
        text = text.replace(matchedStartTimeText.trim(), "");

        Match dueDateMatch = dueDateMatcher.match(text);
        String matchedDueDateText = dueDateMatch != null ? dueDateMatch.text : "";
        text = text.replace(matchedDueDateText.trim(), "");

        Match recurrentMatch = recurrenceMatcher.match(text);
        String matchedRecurrenceText = recurrentMatch != null ? recurrentMatch.text : "";
        text = text.replace(matchedRecurrenceText.trim(), "");

        Match timesPerDayMatch = timesPerDayMatcher.match(text);
        String matchedTimesPerDayText = timesPerDayMatch != null ? timesPerDayMatch.text : "";
        text = text.replace(matchedTimesPerDayText.trim(), "");

        String name = text.trim();

        if (name.isEmpty()) {
            return null;
        }

        return new RecurrentQuest(rawText);
    }

    public boolean isRecurrent(String text) {
        return recurrenceMatcher.match(text) != null || timesPerDayMatcher.match(text) != null;
    }
}
