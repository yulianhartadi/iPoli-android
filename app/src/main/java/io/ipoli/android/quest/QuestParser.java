package io.ipoli.android.quest;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Date;

import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/19/16.
 */
public class QuestParser {

    private final DueDateMatcher dueDateMatcher;
    private final StartTimeMatcher startTimeMatcher;
    private final DurationMatcher durationMatcher = new DurationMatcher();

    public QuestParser(PrettyTimeParser timeParser) {
        startTimeMatcher = new StartTimeMatcher(timeParser);
        dueDateMatcher = new DueDateMatcher(timeParser);
    }

    public Quest parse(String text) {

        String originalText = text;

        String matchedDurationText = durationMatcher.match(text);
        int duration = durationMatcher.parse(text);
        text = text.replace(matchedDurationText, "");

        String matchedStartTimeText = startTimeMatcher.match(text);
        int startTime = startTimeMatcher.parse(text);
        text = text.replace(matchedStartTimeText, "");

        String matchedDueDateText = dueDateMatcher.match(text);
        Date dueDate = dueDateMatcher.parse(text);
        text = text.replace(matchedDueDateText, "");

        String name = text;

        Quest q = new Quest(name.trim());
        q.setRawText(originalText);
        q.setDuration(duration);
        q.setEndDate(dueDate);
        q.setStartMinute(startTime);
        return q;
    }
}
