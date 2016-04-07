package io.ipoli.android.quest;

import android.text.TextUtils;

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
    private RecurrenceMatcher recurrenceMatcher = new RecurrenceMatcher();
    private TimesPerDayMatcher timesPerDayMatcher = new TimesPerDayMatcher();

    public QuestParser(PrettyTimeParser timeParser) {
        startTimeMatcher = new StartTimeMatcher(timeParser);
        dueDateMatcher = new DueDateMatcher(timeParser);
    }

    public Quest parse(String text) {

        String originalText = text;

        Match durationMatch = durationMatcher.match(text);
        String matchedDurationText = durationMatch != null ? durationMatch.text : "";
        int duration = durationMatcher.parse(text);
        text = text.replace(matchedDurationText, "");

        Match startTimeMatch = startTimeMatcher.match(text);
        String matchedStartTimeText = startTimeMatch != null ? startTimeMatch.text : "";
        int startTime = startTimeMatcher.parse(text);
        text = text.replace(matchedStartTimeText, "");

        Match dueDateMatch = dueDateMatcher.match(text);
        String matchedDueDateText = dueDateMatch != null ? dueDateMatch.text : "";
        Date dueDate = dueDateMatcher.parse(text);
        text = text.replace(matchedDueDateText, "");

        String name = text.trim();

        if (TextUtils.isEmpty(name)) {
            return null;
        }

        Quest q = new Quest(name);
        q.setRawText(originalText);
        q.setDuration(duration);
        q.setEndDate(dueDate);
        q.setStartMinute(startTime);
        return q;
    }

    public RecurrentQuest parseRecurrent(String text) {

        Match durationMatch = durationMatcher.match(text);
        String matchedDurationText = durationMatch != null ? durationMatch.text : "";
        text = text.replace(matchedDurationText, "");

        Match startTimeMatch = startTimeMatcher.match(text);
        String matchedStartTimeText = startTimeMatch != null ? startTimeMatch.text : "";
        text = text.replace(matchedStartTimeText, "");

        Match dueDateMatch = dueDateMatcher.match(text);
        String matchedDueDateText = dueDateMatch != null ? dueDateMatch.text : "";
        text = text.replace(matchedDueDateText, "");

        Match recurrentMatch = recurrenceMatcher.match(text);
        text = text.replace(recurrentMatch.text, "");

        Match timesPerDayMatch = timesPerDayMatcher.match(text);
        text = text.replace(timesPerDayMatch.text, "");

        String name = text.trim();

        if (TextUtils.isEmpty(name)) {
            return null;
        }

        return new RecurrentQuest(text);
    }

    public boolean isRecurrent(String text) {
        return recurrenceMatcher.match(text) != null;
    }
}
