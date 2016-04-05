package io.ipoli.android;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.List;

import io.ipoli.android.quest.suggestions.ParsedPart;
import io.ipoli.android.quest.suggestions.SuggestionType;
import io.ipoli.android.quest.suggestions.SuggestionsManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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

    @Test
    public void parseSpace() {
        String text = " ";
        assertTrue(sm.parse(text, text.length()).isEmpty());
    }

    @Test
    public void parseEmpty() {
        String text = "";
        assertTrue(sm.parse(text, text.length()).isEmpty());
    }

    @Test
    public void deleteDueDate() {
        String preDeleteText = "Work today for ";
        String expectedText = "Work  for ";
        SuggestionsManager.TextTransformResult res = sm.deleteText(preDeleteText, 9);
        assertTransformedResult(res, expectedText, 5);
    }

    @Test
    public void deleteDueDateAndParse() {
        String preDeleteText = "Work today for 1h with";
        String expectedText = "Work  for 1h with";
        SuggestionsManager.TextTransformResult res = sm.deleteText(preDeleteText, 9);
        assertTransformedResult(res, expectedText, 5);

        List<ParsedPart> parts = sm.parse(res.text, res.selectionIndex);
        assertThat(parts.size(), is(1));
        assertParsedPart(parts.get(0),  SuggestionType.DURATION, 6, 11, false);
    }

    @Test
    public void deleteDurationAndParse() {
        String preDeleteText = "Work today for 1h and 1m";
        String expectedText = "Work today for 1h and 1";
        SuggestionsManager.TextTransformResult res = sm.deleteText(preDeleteText, preDeleteText.length() - 1);
        assertTransformedResult(res, expectedText, expectedText.length());
        List<ParsedPart> parts = sm.parse(res.text, res.selectionIndex);
        assertThat(parts.size(), is(2));
        assertParsedPart(parts.get(1),  SuggestionType.DURATION, 11, expectedText.length() - 1, true);
    }

    @Test
    public void deleteNonParsedPart() {
        String preDeleteText = "Work today for ";
        String expectedText = "Work today or ";
        SuggestionsManager.TextTransformResult res = sm.deleteText(preDeleteText, 11);
        assertTransformedResult(res, expectedText, 11);
    }

    @Test
    public void selectionOnParsedPartLeft() {
        String text = "Work today for ";
        int expectedSelectionIndex = 5;
        int selectedIndex = expectedSelectionIndex + 2;
        assertThat(sm.getSelectionIndex(text, selectedIndex), is(expectedSelectionIndex));
    }

    @Test
    public void selectionOnParsedPartRight() {
        String text = "Work today for ";
        int expectedSelectionIndex = 10;
        int selectedIndex = expectedSelectionIndex - 2;
        assertThat(sm.getSelectionIndex(text, selectedIndex), is(expectedSelectionIndex));
    }

    @Test
    public void selectionOnParsedPartMiddle() {
        String text = "Work for 1h at ";
        int expectedSelectionIndex = 11;
        int selectedIndex = expectedSelectionIndex - 3;
        assertThat(sm.getSelectionIndex(text, selectedIndex), is(expectedSelectionIndex));
    }

    @Test
    public void selectionOnParsedPartOnStart() {
        String text = "Today work for ";
        int expectedSelectionIndex = 0;
        int selectedIndex = expectedSelectionIndex + 2;
        assertThat(sm.getSelectionIndex(text, selectedIndex), is(expectedSelectionIndex));
    }

    @Test
    public void selectionOnNonParsedPart() {
        String text = "Work today ";
        int expectedSelectionIndex = 4;
        int selectedIndex = 4;
        assertThat(sm.getSelectionIndex(text, selectedIndex), is(expectedSelectionIndex));
    }

    private void assertParsedPart(ParsedPart part, SuggestionType type, int startIdx, int endIdx, boolean isPartial) {
        assertThat(part.type, is(type));
        assertThat(part.startIdx, is(startIdx));
        assertThat(part.endIdx, is(endIdx));
        assertThat(part.isPartial, is(isPartial));
    }

    private void assertTransformedResult(SuggestionsManager.TextTransformResult res, String text, int selectionIndex) {
        assertThat(res.text, is(text));
        assertThat(res.selectionIndex, is(selectionIndex));
    }
}
