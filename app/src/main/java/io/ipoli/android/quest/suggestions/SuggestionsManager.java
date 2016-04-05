package io.ipoli.android.quest.suggestions;

import android.util.Log;

import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.ipoli.android.app.utils.StringUtils;
import io.ipoli.android.quest.parsers.DueDateMatcher;
import io.ipoli.android.quest.parsers.DurationMatcher;
import io.ipoli.android.quest.parsers.MainMatcher;
import io.ipoli.android.quest.parsers.Match;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;
import io.ipoli.android.quest.suggestions.suggesters.BaseTextSuggester;
import io.ipoli.android.quest.suggestions.suggesters.DueDateTextSuggester;
import io.ipoli.android.quest.suggestions.suggesters.DurationTextSuggester;
import io.ipoli.android.quest.suggestions.suggesters.MainTextSuggester;
import io.ipoli.android.quest.suggestions.suggesters.RecurrenceTextSuggester;
import io.ipoli.android.quest.suggestions.suggesters.StartTimeTextSuggester;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/16.
 */
public class SuggestionsManager {

    private Map<SuggestionType, QuestTextMatcher> typeToMatcher;
    SuggestionType currentType;
    Map<SuggestionType, BaseTextSuggester> textSuggesters = new HashMap<>();
    Set<SuggestionType> usedTypes = new HashSet<>();
    List<SuggestionType> orderedSuggestionTypes = new ArrayList<SuggestionType>() {{
        add(SuggestionType.DURATION);
        add(SuggestionType.START_TIME);
        add(SuggestionType.DUE_DATE);
        add(SuggestionType.TIMES_PER_DAY);
        add(SuggestionType.RECURRENT);
//        add(SuggestionType.MAIN);
    }};

//    List<ParsedPart> parsedParts = new ArrayList<>();

    OnSuggestionsUpdatedListener suggestionsUpdatedListener;

    public SuggestionsManager(PrettyTimeParser parser) {
        currentType = SuggestionType.MAIN;

        textSuggesters.put(SuggestionType.MAIN, new MainTextSuggester());
        textSuggesters.put(SuggestionType.DUE_DATE, new DueDateTextSuggester(parser));
        textSuggesters.put(SuggestionType.DURATION, new DurationTextSuggester());
        textSuggesters.put(SuggestionType.START_TIME, new StartTimeTextSuggester(parser));
        textSuggesters.put(SuggestionType.RECURRENT, new RecurrenceTextSuggester());
        textSuggesters.put(SuggestionType.TIMES_PER_DAY, new TimesPerDayTextSuggester());

        typeToMatcher = new HashMap<SuggestionType, QuestTextMatcher>() {{
            put(SuggestionType.DURATION, new DurationMatcher());
            put(SuggestionType.START_TIME, new StartTimeMatcher(parser));
            put(SuggestionType.DUE_DATE, new DueDateMatcher(parser));
            put(SuggestionType.TIMES_PER_DAY, new TimesPerDayMatcher());
            put(SuggestionType.RECURRENT, new RecurrenceMatcher());
            put(SuggestionType.MAIN, new MainMatcher());
        }};
    }

    public List<ParsedPart> onTextChange(String text, int selectionIndex) {
        List<ParsedPart> parsedParts = parse(text, selectionIndex);
        ParsedPart partialPart = findPartialPart(parsedParts);
        if (partialPart != null) {
            changeCurrentSuggester(partialPart.type);
        } else {
            changeCurrentSuggester(SuggestionType.MAIN);
        }
        return parsedParts;
    }

    public List<ParsedPart> parse(String text) {
        return parse(text, text.length());
    }

    public List<ParsedPart> parse(String text, int selectionIndex) {
        List<ParsedPart> parts = new ArrayList<>();
        for (SuggestionType t : orderedSuggestionTypes) {
            QuestTextMatcher mather = typeToMatcher.get(t);
            Match partialMatch = mather.partialMatch(text.substring(0, selectionIndex));
            if (partialMatch != null && findPartialPart(parts) == null && !usedTypes.contains(t)) {
                int start = partialMatch.text.startsWith(" ") ? partialMatch.start + 1 : partialMatch.start;
                int end = partialMatch.end;
                parts.add(new ParsedPart(start, end, t, true));
            } else {
                Match match = typeToMatcher.get(t).match(text);
                if (match != null) {
                    if (t == SuggestionType.MAIN) {
                        break;
                    }
                    usedTypes.add(t);
                    int start = match.text.startsWith(" ") ? match.start + 1 : match.start;
                    int end = match.text.endsWith(" ") ? match.end - 1 : match.end;
                    parts.add(new ParsedPart(start, end, t, false));
                } else {
                    usedTypes.remove(t);
                }
            }
        }
        Collections.sort(parts, new Comparator<ParsedPart>() {

            @Override
            public int compare(ParsedPart lhs, ParsedPart rhs) {
                return lhs.startIdx < rhs.startIdx ? -1 : 1;
            }
        });

        return parts;
    }

    public TextTransformResult deleteText(String preDeleteText, int deleteStartIndex) {
        ParsedPart partToDelete = findNotPartialParsedPartContainingIdx(deleteStartIndex, parse(preDeleteText, deleteStartIndex));
        if (partToDelete == null) {
            return new TextTransformResult(StringUtils.cut(preDeleteText, deleteStartIndex, deleteStartIndex), deleteStartIndex);
        }
        usedTypes.remove(partToDelete.type);
        return new TextTransformResult(StringUtils.cut(preDeleteText, partToDelete.startIdx, partToDelete.endIdx), partToDelete.startIdx);
    }

    public int getSelectionIndex(String text, int selectedIndex) {
        ParsedPart p = findNotPartialParsedPartContainingIdx(selectedIndex, parse(text));
        if (p == null) {
            return selectedIndex;
        }
        if (selectedIndex - p.startIdx < (p.endIdx + 1) - selectedIndex) {
            return p.startIdx;
        } else {
            return Math.min(p.endIdx + 1, text.length() - 1);
        }
    }

    public TextTransformResult replace(String text, String replaceText, int selectionIndex) {
        ParsedPart parsedPart = findPartialPart(parse(text, selectionIndex));
        String start, end;
        if (parsedPart == null) {
            start = text.substring(0, selectionIndex);
            end = text.substring(selectionIndex);
        } else {
            start = text.substring(0, parsedPart.startIdx);
            end = parsedPart.endIdx + 1 < text.length() ? text.substring(parsedPart.endIdx + 1) : "";
        }

        replaceText += " ";
        start = start.isEmpty() || start.endsWith(" ") ? start : start + " ";
        end = end.isEmpty() || end.startsWith(" ") ? end : " " + end;
        return new TextTransformResult(start + replaceText + end, (start + replaceText).length());
    }


    public void changeCurrentSuggester(SuggestionType type) {
        if (type == currentType) {
            return;
        }
        currentType = type;
        suggestionsUpdatedListener.onSuggestionsUpdated();
    }

    private ParsedPart findPartialPart(List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.isPartial) {
                return p;
            }
        }
        return null;
    }

    public void setSuggestionsUpdatedListener(OnSuggestionsUpdatedListener suggestionsUpdatedListener) {
        this.suggestionsUpdatedListener = suggestionsUpdatedListener;
    }

    public List<SuggestionDropDownItem> getSuggestions() {
        return getCurrentSuggester().getSuggestions();
    }

    public List<SuggestionType> getUnusedTypes() {
        List<SuggestionType> types = new ArrayList<>();
        for (SuggestionType t : orderedSuggestionTypes) {
            if (!usedTypes.contains(t)) {
                types.add(t);
            }
        }
        return types;
    }

    public void changeCurrentSuggester(SuggestionType type, int startIdx, int length) {
        if (type == currentType) {
            return;
        }
        Log.d("ChangeCurrentSuggester", "From: " + currentType.name()
                + " To: " + type.name() + " startIdx: " + startIdx + " length: " + length);
        currentType = type;
        getCurrentSuggester().setStartIdx(startIdx);
        getCurrentSuggester().setLength(length);
        suggestionsUpdatedListener.onSuggestionsUpdated();
    }

    public BaseTextSuggester getCurrentSuggester() {
        return textSuggesters.get(currentType);
    }

    public ParsedPart findNotPartialParsedPartContainingIdx(int index, List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.startIdx <= index && index <= p.endIdx && !p.isPartial) {
                return p;
            }
        }
        return null;
    }

    private ParsedPart findNotPartialParsedPartContainingOrNextToIdx(int index, List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.startIdx - 1 <= index && index <= p.endIdx + 1 && !p.isPartial) {
                return p;
            }
        }
        return null;
    }


    public class TextTransformResult {
        public String text;
        public int selectionIndex;

        public TextTransformResult(String text, int selectionIndex) {
            this.text = text;
            this.selectionIndex = selectionIndex;
        }
    }

//    private List<AddQuestSuggestion> getRecurrentDayOfWeekSuggestions() {
//        int icon = R.drawable.ic_event_black_18dp;
//        List<AddQuestSuggestion> suggestions = new ArrayList<>();
//        suggestions.add(new AddQuestSuggestion(icon, "Monday", "Mon"));
//        suggestions.add(new AddQuestSuggestion(icon, "Tuesday", "Tue"));
//        suggestions.add(new AddQuestSuggestion(icon, "Wednesday", "Wed"));
//        suggestions.add(new AddQuestSuggestion(icon, "Thursday", "Thur"));
//        suggestions.add(new AddQuestSuggestion(icon, "Friday", "Fri"));
//        suggestions.add(new AddQuestSuggestion(icon, "Saturday", "Sat"));
//        suggestions.add(new AddQuestSuggestion(icon, "Sunday", "Sun"));
//        return suggestions;
//    }

//    private List<AddQuestSuggestion> getRecurrentDayOfMonthSuggestions() {
//        int icon = R.drawable.ic_event_black_18dp;
//        List<AddQuestSuggestion> suggestions = new ArrayList<>();
//        suggestions.add(new AddQuestSuggestion(icon, "21st of the month"));
//        suggestions.add(new AddQuestSuggestion(icon, "22nd of the month"));
//        return suggestions;
//    }

}
