package io.ipoli.android.quest.suggestions;

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
import io.ipoli.android.quest.parsers.Match;
import io.ipoli.android.quest.parsers.QuestTextMatcher;
import io.ipoli.android.quest.parsers.RecurrenceMatcher;
import io.ipoli.android.quest.parsers.StartTimeMatcher;
import io.ipoli.android.quest.parsers.TimesPerDayMatcher;
import io.ipoli.android.quest.suggestions.providers.DayOfMonthSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.DayOfWeekSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.DueDateSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.DurationSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.MainSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.RecurrenceSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.StartTimeSuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.SuggestionsProvider;
import io.ipoli.android.quest.suggestions.providers.TimesPerDayTextSuggestiosProvider;

/**
 * Created by Polina Zhelyazkova <polina@ipoli.io>
 * on 3/24/16.
 */
public class SuggestionsManager {

    private Map<TextEntityType, QuestTextMatcher> typeToMatcher;
    TextEntityType currentType;
    Map<TextEntityType, SuggestionsProvider> textSuggestionsProvider = new HashMap<>();
    Set<TextEntityType> usedTypes = new HashSet<>();
    List<TextEntityType> orderedTextEntityTypes = new ArrayList<TextEntityType>() {{
        add(TextEntityType.DURATION);
        add(TextEntityType.START_TIME);
        add(TextEntityType.DUE_DATE);
        add(TextEntityType.TIMES_PER_DAY);
        add(TextEntityType.RECURRENT);
    }};
    private List<SuggestionDropDownItem> currentSuggestions = new ArrayList<>();

    OnSuggestionsUpdatedListener suggestionsUpdatedListener;

    public SuggestionsManager(PrettyTimeParser parser) {

        textSuggestionsProvider.put(TextEntityType.MAIN, new MainSuggestionsProvider());
        textSuggestionsProvider.put(TextEntityType.DUE_DATE, new DueDateSuggestionsProvider());
        textSuggestionsProvider.put(TextEntityType.DURATION, new DurationSuggestionsProvider());
        textSuggestionsProvider.put(TextEntityType.START_TIME, new StartTimeSuggestionsProvider());
        textSuggestionsProvider.put(TextEntityType.RECURRENT, new RecurrenceSuggestionsProvider());
        textSuggestionsProvider.put(TextEntityType.TIMES_PER_DAY, new TimesPerDayTextSuggestiosProvider());
        textSuggestionsProvider.put(TextEntityType.RECURRENT_DAY_OF_WEEK, new DayOfWeekSuggestionsProvider());
        textSuggestionsProvider.put(TextEntityType.RECURRENT_DAY_OF_MONTH, new DayOfMonthSuggestionsProvider());

        typeToMatcher = new HashMap<TextEntityType, QuestTextMatcher>() {{
            put(TextEntityType.DURATION, new DurationMatcher());
            put(TextEntityType.START_TIME, new StartTimeMatcher(parser));
            put(TextEntityType.DUE_DATE, new DueDateMatcher(parser));
            put(TextEntityType.TIMES_PER_DAY, new TimesPerDayMatcher());
            put(TextEntityType.RECURRENT, new RecurrenceMatcher());
        }};
        changeCurrentSuggestionsProvider(TextEntityType.MAIN, "");
    }

    public List<ParsedPart> onTextChange(String text, int selectionIndex) {
        return onTextChange(text, selectionIndex, true);
    }

    public List<ParsedPart> onTextChange(String text, int selectionIndex, boolean changeState) {
        List<ParsedPart> parsedParts = parse(text, selectionIndex);
        ParsedPart partialPart = findPartialPart(parsedParts);

        TextEntityType newType = currentType;
        String parsedTypeText = "";
        if (changeState) {
            newType = partialPart != null ? partialPart.type : TextEntityType.MAIN;
            parsedTypeText = partialPart != null ? StringUtils.substring(text, partialPart.startIdx, partialPart.endIdx) : "";
        }
        changeCurrentSuggestionsProvider(newType, parsedTypeText);
        return parsedParts;
    }

    public List<ParsedPart> parse(String text) {
        return parse(text, text.length());
    }

    public List<ParsedPart> parse(String text, int selectionIndex) {
        List<ParsedPart> parts = new ArrayList<>();
        for (TextEntityType t : orderedTextEntityTypes) {
            QuestTextMatcher mather = typeToMatcher.get(t);
            Match partialMatch = mather.partialMatch(text.substring(0, selectionIndex));
            if (partialMatch != null && findPartialPart(parts) == null && !usedTypes.contains(t)) {
                int start = partialMatch.text.startsWith(" ") ? partialMatch.start + 1 : partialMatch.start;
                int end = partialMatch.end;
                parts.add(new ParsedPart(start, end, t, true));
            } else {
                Match match = typeToMatcher.get(t).match(text);
                if (match != null) {
                    if (t == TextEntityType.MAIN) {
                        break;
                    }
                    addUsedType(t);
                    int start = match.text.startsWith(" ") ? match.start + 1 : match.start;
                    int end = match.text.endsWith(" ") ? match.end - 1 : match.end;
                    parts.add(new ParsedPart(start, end, t, false));
                } 
            }
        }
        Collections.sort(parts, new Comparator<ParsedPart>() {

            @Override
            public int compare(ParsedPart lhs, ParsedPart rhs) {
                return lhs.startIdx < rhs.startIdx ? -1 : 1;
            }
        });

        if(parsedPartsContainsType(TextEntityType.DUE_DATE, parts) && parsedPartsContainsType(TextEntityType.RECURRENT, parts)) {
            ParsedPart dueDate = findParsedPartByType(TextEntityType.DUE_DATE, parts);
            ParsedPart recurrent = findParsedPartByType(TextEntityType.RECURRENT, parts);
            if(dueDate.startIdx < recurrent.startIdx) {
                parts.remove(recurrent);
                removeUsedType(TextEntityType.RECURRENT);
            } else {
                parts.remove(dueDate);
                removeUsedType(TextEntityType.DUE_DATE);
            }
        }

        if(parsedPartsContainsType(TextEntityType.START_TIME, parts) && parsedPartsContainsType(TextEntityType.TIMES_PER_DAY, parts)) {
            ParsedPart startTime = findParsedPartByType(TextEntityType.START_TIME, parts);
            ParsedPart timesPerDay = findParsedPartByType(TextEntityType.TIMES_PER_DAY, parts);
            if(startTime.startIdx < timesPerDay.startIdx) {
                parts.remove(timesPerDay);
                removeUsedType(TextEntityType.TIMES_PER_DAY);
            } else {
                parts.remove(startTime);
                removeUsedType(TextEntityType.START_TIME);
            }
        }

        return parts;
    }

    public TextTransformResult deleteText(String preDeleteText, int deleteStartIndex) {
        ParsedPart partToDelete = findNotPartialParsedPartContainingIdx(deleteStartIndex, parse(preDeleteText, deleteStartIndex));
        if (partToDelete == null) {
            return new TextTransformResult(StringUtils.cut(preDeleteText, deleteStartIndex, deleteStartIndex), deleteStartIndex);
        }
        removeUsedType(partToDelete.type);

        return new TextTransformResult(StringUtils.cut(preDeleteText, partToDelete.startIdx, partToDelete.endIdx), partToDelete.startIdx);
    }

    public TextTransformResult onSuggestionItemClick(String text, SuggestionDropDownItem suggestion, int selectionIndex) {
        TextTransformResult result;
        if (suggestion.shouldReplace) {
            result = replace(text, suggestion.text, selectionIndex);
            addUsedType(currentType);
        } else {
            result = append(text, suggestion.text, selectionIndex);
        }
        return result;
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
        ParsedPart partialPart = findPartialPart(parse(text, selectionIndex));
        String start, end;
        if (partialPart == null) {
            start = text.substring(0, selectionIndex);
            end = text.substring(selectionIndex);
        } else {
            start = text.substring(0, partialPart.startIdx);
            end = partialPart.endIdx + 1 < text.length() ? text.substring(partialPart.endIdx + 1) : "";
        }

        replaceText += replaceText.isEmpty() ? "" : " ";
        start = start.isEmpty() || start.endsWith(" ") ? start : start + " ";
        end = end.isEmpty() || end.startsWith(" ") ? end : " " + end;
        return new TextTransformResult(start + replaceText + end, (start + replaceText).length());
    }

    public TextTransformResult append(String text, String appendText, int selectionIndex) {
        String start = text.substring(0, selectionIndex);
        String end = text.substring(selectionIndex);
        appendText += appendText.isEmpty() ? "" : " ";
        start = start.isEmpty() || start.endsWith(" ") ? start : start + " ";
        end = end.isEmpty() || end.startsWith(" ") ? end : " " + end;
        return new TextTransformResult(start + appendText + end, (start + appendText).length());
    }

    public void changeCurrentSuggestionsProvider(TextEntityType type, String parsedText) {
        currentType = type;
        currentSuggestions = getCurrentSuggestionsProvider().filter(parsedText);
        if (suggestionsUpdatedListener != null) {
            suggestionsUpdatedListener.onSuggestionsUpdated();
        }
    }

    public void setSuggestionsUpdatedListener(OnSuggestionsUpdatedListener suggestionsUpdatedListener) {
        this.suggestionsUpdatedListener = suggestionsUpdatedListener;
    }

    public List<SuggestionDropDownItem> getSuggestions() {
        return currentSuggestions;
    }

    public SuggestionsProvider getCurrentSuggestionsProvider() {
        return textSuggestionsProvider.get(currentType);
    }

    public TextEntityType getCurrentSuggestionsProviderType() {
        return currentType;
    }

    private ParsedPart findPartialPart(List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.isPartial) {
                return p;
            }
        }
        return null;
    }

    private ParsedPart findParsedPartByType(TextEntityType type, List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.type == type) {
                return p;
            }
        }
        return null;
    }

    private boolean parsedPartsContainsType(TextEntityType type, List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.type == type) {
                return true;
            }
        }
        return false;
    }

    private ParsedPart findNotPartialParsedPartContainingIdx(int index, List<ParsedPart> parsedParts) {
        for (ParsedPart p : parsedParts) {
            if (p.startIdx <= index && index <= p.endIdx && !p.isPartial) {
                return p;
            }
        }
        return null;
    }


    private void addUsedType(TextEntityType type) {
        if (type == TextEntityType.MAIN) {
            return;
        }
        usedTypes.add(type);
        ((MainSuggestionsProvider) textSuggestionsProvider.get(TextEntityType.MAIN)).addUsedTextEntityType(type);
    }

    private void removeUsedType(TextEntityType type) {
        usedTypes.remove(type);
        ((MainSuggestionsProvider) textSuggestionsProvider.get(TextEntityType.MAIN)).removeUsedTextEntityType(type);
    }

    public class TextTransformResult {
        public String text;
        public int selectionIndex;

        public TextTransformResult(String text, int selectionIndex) {
            this.text = text;
            this.selectionIndex = selectionIndex;
        }
    }

}
