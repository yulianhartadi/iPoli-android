package io.ipoli.android;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.List;

import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionType;
import io.ipoli.android.quest.suggestions.SuggestionsManager;

import static org.junit.Assert.assertTrue;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/4/16.
 */
public class SuggestionManagerTest {
    private static PrettyTimeParser parser;
    private static SuggestionsManager sm;

    @BeforeClass
    public static void setUp() {
        parser = new PrettyTimeParser();
    }

    @Before
    public void beforeEach() {
        sm = new SuggestionsManager(parser);
    }

    @Test
    public void parseWithoutParts() {
        String text = "Work";
        assertTrue(sm.parse(text, text.length()).isEmpty());
    }

    @Test
    public void parseDueDatePart() {
        String dueDate = "today";
        String text = "Work " + dueDate;
        assertTrue(sm.parse(text, text.length()).size() == 1);
        assertParsedPart(sm.parse(text, text.length()).get(0), SuggestionType.DUE_DATE, text.indexOf(dueDate), text.indexOf(dueDate) + dueDate.length() - 1, true);
    }

    @Test
    public void parseDueDatePartWithEndingSpace() {
        String dueDate = "on 12 Feb ";
        String text = "Work " + dueDate;
        List<ParsedPart> parts = sm.parse(text, text.length());
        assertTrue(parts.size() == 1);
        assertParsedPart(parts.get(0), SuggestionType.DUE_DATE, text.indexOf(dueDate), text.indexOf(dueDate) + dueDate.length() - 2, false);
    }

    @Test
    public void parseDueDateAndStartTimeParts() {
        String dueDate = "after 3 days";
        String startTime = "at 7pm ";
        String text = "Work " + dueDate + " " + startTime;
        List<ParsedPart> parts = sm.parse(text, text.length());
        assertTrue(parts.size() == 2);
        assertParsedPart(parts.get(0), SuggestionType.DUE_DATE, text.indexOf(dueDate), text.indexOf(dueDate) + dueDate.length() - 1, false);
        assertParsedPart(parts.get(1), SuggestionType.START_TIME, text.indexOf(startTime), text.indexOf(startTime) + startTime.length() - 2, false);
    }

    @Test
    public void parseDueDateAndPartialStartTimeParts() {
        String dueDate = "after 3 days";
        String startTime = "at 7pm";
        String text = "Work " + dueDate + " " + startTime;
        List<ParsedPart> parts = sm.parse(text, text.length());
        assertTrue(parts.size() == 2);
        assertParsedPart(parts.get(0), SuggestionType.DUE_DATE, text.indexOf(dueDate), text.indexOf(dueDate) + dueDate.length() - 1, false);
        assertParsedPart(parts.get(1), SuggestionType.START_TIME, text.indexOf(startTime), text.indexOf(startTime) + startTime.length() - 1, true);
    }

    @Test
    public void parseWithoutPartsIncludingMain() {
        String text = "Work on presentation";
        assertTrue(sm.parse(text, text.length()).isEmpty());
    }

    @Test
    public void parsePartialDuration() {
        String duration = "for 30m";
        String text = "Work on presentation " + duration;
        List<ParsedPart> parts = sm.parse(text, text.length());
        assertTrue(parts.size() == 1);
        assertParsedPart(parts.get(0), SuggestionType.DURATION, text.indexOf(duration), text.indexOf(duration) + duration.length() - 1, true);
    }

    @Test
    public void parseOnlyPartialDuration() {
        String duration = "for 30m";
        String text = "Work on 12 presentations " + duration;
        List<ParsedPart> parts = sm.parse(text, text.length());
        assertTrue(parts.size() == 1);
        assertParsedPart(parts.get(0), SuggestionType.DURATION, text.indexOf(duration), text.indexOf(duration) + duration.length() - 1, true);
    }

    @Test
    public void parsePartialDueDate() {
        String dueDate = "on 12";
        String text = "Work " + dueDate;
        List<ParsedPart> parts = sm.parse(text, text.length());
        assertTrue(parts.size() == 1);
        assertParsedPart(parts.get(0), SuggestionType.DUE_DATE, text.indexOf(dueDate), text.indexOf(dueDate) + dueDate.length() - 1, true);
    }

    private void assertParsedPart(ParsedPart part, SuggestionType type, int startIdx, int endIdx, boolean isPartial) {
        assertTrue(part.type == type);
        assertTrue(part.startIdx == startIdx);
        assertTrue(part.endIdx == endIdx);
        assertTrue(part.isPartial == isPartial);
    }
}
